package net.dodian.uber.game.ui

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dodian.uber.game.engine.loop.GameThreadIngress
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.SendString
import net.dodian.uber.game.netty.listener.out.ShowInterface
import net.dodian.uber.game.netty.listener.out.SendEnterName
import net.dodian.uber.game.persistence.DbDispatchers
import net.dodian.uber.game.persistence.repository.DbAsyncRepository
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date

object AccountServices {
    private val logger = LoggerFactory.getLogger(AccountServices::class.java)

    @JvmStatic
    fun open(client: Client) {
        client.viewingAccountServices = true
        client.accountPasswordState = 0

        // Account Status first, Change Password second
        client.send(SendString("Account Status", 36732))
        client.send(SendString("Change Password", 36734))

        // Clear remaining slots (36736 to 36770)
        for (stringId in 36736..36770 step 2) {
            client.send(SendString("", stringId))
        }

        // Title
        client.send(SendString("Account Services", 36705))

        // Show player details initially
        showDefaultStatus(client)

        client.send(ShowInterface(36700))
    }

    @JvmStatic
    fun handleRowClick(client: Client, index: Int) {
        if (index == 0) { // Account Status
            showDefaultStatus(client)
        } else if (index == 1) { // Change Password
            client.accountPasswordState = 1
            client.send(SendString("Change Password", 36706))
            client.send(SendString("Step 1:", 36707))
            client.send(SendString("Type current password in prompt", 36708))
            client.send(SendString("Step 2:", 36709))
            client.send(SendString("Type new password in prompt", 36710))
            client.send(SendString("Status:", 36711))
            client.send(SendString("Awaiting current password...", 36712))
            client.send(SendString("", 36713))
            client.send(SendString("", 36714))
            client.send(SendEnterName("Enter current password:"))
        }
    }

    @JvmStatic
    fun verifyCurrentPassword(client: Client, inputPass: String) {
        val token = ++client.accountServiceRequestToken
        GlobalScope.launch(DbDispatchers.accountDispatcher) {
            try {
                val verified = DbAsyncRepository.withConnection { conn ->
                    try {
                        val ps = conn.prepareStatement("SELECT password, salt FROM user WHERE LOWER(username) = ?")
                        ps.setString(1, client.playerName.lowercase())
                        val rs = ps.executeQuery()
                        if (rs.next()) {
                            val dbPass = rs.getString("password")
                            var salt = rs.getString("salt")
                            if (salt == null) salt = ""
                            val hashedInput = Client.passHash(inputPass, salt)
                            hashedInput.equals(dbPass, ignoreCase = true)
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        logger.warn("Database error verifying password for {}", client.playerName, e)
                        null
                    }
                }
                GameThreadIngress.submitCritical("account-verify") {
                    if (client.accountServiceRequestToken != token) return@submitCritical
                    if (client.accountPasswordState != 1) return@submitCritical

                    when (verified) {
                        true -> {
                            client.accountPasswordState = 2
                            client.send(SendString("<col=00ff00>Current password verified.</col>", 36712))
                            client.send(SendString("Awaiting new password...", 36714))
                            client.send(SendEnterName("Enter new password:"))
                        }
                        false -> {
                            client.send(SendString("<col=ff0000>Incorrect current password.</col>", 36712))
                            client.sendMessage("Incorrect current password. Password change aborted.")
                        }
                        null -> {
                            client.send(SendString("<col=ff0000>Database error.</col>", 36712))
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error verifying password for {}", client.playerName, e)
                GameThreadIngress.submitCritical("account-verify-error") {
                    if (client.accountServiceRequestToken != token) return@submitCritical
                    client.send(SendString("<col=ff0000>Error verifying password.</col>", 36712))
                }
            }
        }
    }

    @JvmStatic
    fun changePassword(client: Client, newPassword: String) {
        if (newPassword.isEmpty() || newPassword.length > 20) {
            client.send(SendString("<col=ff0000>Password must be 1-20 chars.</col>", 36712))
            client.sendMessage("Invalid password. Password must be 1-20 characters long.")
            return
        }

        val token = ++client.accountServiceRequestToken
        GlobalScope.launch(DbDispatchers.accountDispatcher) {
            try {
                val updated = DbAsyncRepository.withConnection { conn ->
                    try {
                        val ps = conn.prepareStatement("SELECT salt FROM user WHERE LOWER(username) = ?")
                        ps.setString(1, client.playerName.lowercase())
                        val rs = ps.executeQuery()
                        if (rs.next()) {
                            var salt = rs.getString("salt")
                            if (salt == null) salt = ""
                            val hashedPass = Client.passHash(newPassword, salt)

                            val psUpdate = conn.prepareStatement("UPDATE user SET password = ? WHERE LOWER(username) = ?")
                            psUpdate.setString(1, hashedPass)
                            psUpdate.setString(2, client.playerName.lowercase())
                            psUpdate.executeUpdate() > 0
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        logger.warn("Database error changing password for {}", client.playerName, e)
                        null
                    }
                }
                GameThreadIngress.submitCritical("account-changepw") {
                    if (client.accountServiceRequestToken != token) return@submitCritical
                    when (updated) {
                        true -> {
                            client.send(SendString("<col=00ff00>Password updated successfully!</col>", 36712))
                            client.send(SendString("", 36714))
                            client.sendMessage("Your password has been successfully updated!")
                        }
                        false -> {
                            client.send(SendString("<col=ff0000>Failed to update password.</col>", 36712))
                            client.send(SendString("", 36714))
                        }
                        null -> {
                            client.send(SendString("<col=ff0000>Database error.</col>", 36712))
                            client.send(SendString("", 36714))
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error changing password for {}", client.playerName, e)
                GameThreadIngress.submitCritical("account-changepw-error") {
                    if (client.accountServiceRequestToken != token) return@submitCritical
                    client.send(SendString("<col=ff0000>Error changing password.</col>", 36712))
                    client.send(SendString("", 36714))
                }
            }
        }
    }

    @JvmStatic
    fun handleAccountServicesDialogue(client: Client, option: Int) {
        if (client.accountServicesDialogState == 3) {
            client.accountServicesDialogState = 0
            client.send(net.dodian.uber.game.netty.listener.out.RemoveInterfaces())
        }
    }

    @JvmStatic
    fun showDefaultStatus(client: Client) {
        val now = System.currentTimeMillis()
        val muteStatus = if (client.mutedTill <= now) {
            "Not Muted"
        } else {
            val remainingMs = client.mutedTill - now
            val hours = remainingMs / 3600000L
            val minutes = (remainingMs % 3600000L) / 60000L
            "Muted (${hours}h ${minutes}m)"
        }

        val token = ++client.accountServiceRequestToken
        GlobalScope.launch(DbDispatchers.accountDispatcher) {
            try {
                val status = DbAsyncRepository.withConnection { conn ->
                    try {
                        val query = "SELECT c.name, u.joindate, u.usergroupid " +
                                       "FROM characters c " +
                                       "LEFT JOIN user u ON LOWER(c.name) = LOWER(u.username) " +
                                       "WHERE LOWER(c.name) = ?"
                        val ps = conn.prepareStatement(query)
                        ps.setString(1, client.playerName.lowercase())
                        val rs = ps.executeQuery()
                        var createdStr = "N/A"
                        var rankStr = "Player"
                        if (rs.next()) {
                            val joinSeconds = rs.getLong("joindate")
                            if (joinSeconds > 0) {
                                val sdf = SimpleDateFormat("yyyy-MM-dd")
                                createdStr = sdf.format(Date(joinSeconds * 1000L))
                            }
                            val mgroup = rs.getInt("usergroupid")
                            val rights = if (mgroup == 9 || mgroup == 5) 1 else if (mgroup == 6 || mgroup == 18 || mgroup == 10) 2 else 0
                            rankStr = if (rights == 1) "Moderator" else if (rights >= 2) "Administrator" else "Player"
                        }
                        Triple(client.playerName, rankStr, createdStr)
                    } catch (e: Exception) {
                        logger.warn("Database error loading account status for {}", client.playerName, e)
                        null
                    }
                }
                GameThreadIngress.submitCritical("account-status") {
                    if (client.accountServiceRequestToken != token) return@submitCritical
                    if (status != null) {
                        val (name, rank, created) = status
                        client.send(SendString(name, 36706))
                        client.send(SendString("Rank:", 36707))
                        client.send(SendString(rank, 36708))
                        client.send(SendString("Created:", 36709))
                        client.send(SendString(created, 36710))
                        client.send(SendString("Mute Status:", 36711))
                        client.send(SendString(muteStatus, 36712))
                        client.send(SendString("IP Address:", 36713))
                        client.send(SendString(client.connectedFrom, 36714))
                    } else {
                        fallbackStatusDisplay(client, muteStatus)
                    }
                }
            } catch (e: Exception) {
                GameThreadIngress.submitCritical("account-status-error") {
                    if (client.accountServiceRequestToken != token) return@submitCritical
                    fallbackStatusDisplay(client, muteStatus)
                }
            }
        }
    }

    private fun fallbackStatusDisplay(client: Client, muteStatus: String) {
        client.send(SendString(client.playerName, 36706))
        client.send(SendString("Rank:", 36707))
        client.send(SendString("Player", 36708))
        client.send(SendString("Created:", 36709))
        client.send(SendString("N/A", 36710))
        client.send(SendString("Mute Status:", 36711))
        client.send(SendString(muteStatus, 36712))
        client.send(SendString("IP Address:", 36713))
        client.send(SendString(client.connectedFrom, 36714))
    }
}
