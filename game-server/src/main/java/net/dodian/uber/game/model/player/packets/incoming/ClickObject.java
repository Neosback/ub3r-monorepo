package net.dodian.uber.game.model.player.packets.incoming;

import net.dodian.cache.object.GameObjectData;
import net.dodian.cache.object.GameObjectDef;
import net.dodian.uber.game.Constants;
import net.dodian.uber.game.Server;
import net.dodian.uber.game.event.Event;
import net.dodian.uber.game.event.EventManager;
import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.WalkToTask;
import net.dodian.uber.game.model.entity.Entity;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.entity.player.PlayerHandler;
import net.dodian.uber.game.model.item.Equipment;
import net.dodian.uber.game.model.object.DoorHandler;
import net.dodian.uber.game.model.object.GlobalObject;
import net.dodian.uber.game.model.object.Object;
import net.dodian.uber.game.model.object.RS2Object;
import net.dodian.uber.game.model.player.packets.Packet;
import net.dodian.uber.game.model.player.packets.outgoing.SendMessage;
import net.dodian.uber.game.model.player.skills.agility.Agility;
import net.dodian.uber.game.model.player.skills.Skill;
import net.dodian.uber.game.model.player.skills.thieving.Thieving;
import net.dodian.uber.game.model.player.skills.agility.Werewolf;
import net.dodian.uber.game.party.Balloons;
import net.dodian.uber.game.security.ItemLog;
import net.dodian.utilities.Misc;
import net.dodian.utilities.Utils;

import java.util.Objects;

import static net.dodian.utilities.DotEnvKt.getGameWorldId;

public class ClickObject implements Packet {

    @Override
    public void ProcessPacket(Client client, int packetType, int packetSize) {
        int objectX = client.getInputStream().readSignedWordBigEndianA();
        int objectID = client.getInputStream().readUnsignedWord();
        int objectY = client.getInputStream().readUnsignedWordA();

        final WalkToTask task = new WalkToTask(WalkToTask.Action.OBJECT_FIRST_CLICK, objectID,
                new Position(objectX, objectY));
        GameObjectDef def = Misc.getObject(objectID, objectX, objectY, client.getPosition().getZ());
        GameObjectData object = GameObjectData.forId(task.getWalkToId());
        client.setWalkToTask(task);
        if (getGameWorldId() > 1 && object != null)
            client.send(new SendMessage("Obj click1: " + object.getId() + ", " + object.getName() + ", Coord: " + objectX + ", " + objectY + ", " + (def == null ? "Def is null!" : def.getFace())));
        if (client.randomed || client.UsingAgility) {
            return;
        }
        EventManager.getInstance().registerEvent(new Event(600) {

            @Override
            public void execute() {

                if (client.disconnected) {
                    this.stop();
                    return;
                }

                if (client.getWalkToTask() != task) {
                    this.stop();
                    return;
                }
                Position objectPosition = null;
                Object o = new Object(objectID, task.getWalkToPosition().getX(), task.getWalkToPosition().getY(), task.getWalkToPosition().getZ(), 10);
                if (def != null && !GlobalObject.hasGlobalObject(o)) {
                    objectPosition = Misc.goodDistanceObject(task.getWalkToPosition().getX(), task.getWalkToPosition().getY(), client.getPosition().getX(), client.getPosition().getY(), Objects.requireNonNull(object).getSizeX(def.getFace()), object.getSizeY(def.getFace()), client.getPosition().getZ());
                } else {
                    if (GlobalObject.hasGlobalObject(o)) {
                        objectPosition = Misc.goodDistanceObject(task.getWalkToPosition().getX(), task.getWalkToPosition().getY(), client.getPosition().getX(), client.getPosition().getY(), Objects.requireNonNull(object).getSizeX(o.face), object.getSizeY(o.type), o.z);
                    } else if (object != null) {
                        objectPosition = Misc.goodDistanceObject(task.getWalkToPosition().getX(), task.getWalkToPosition().getY(), client.getPosition().getX(), client.getPosition().getY(), object.getSizeX(), object.getSizeY(), client.getPosition().getZ());
                    }
                }
                if (objectID == 23131)
                    objectPosition = Misc.goodDistanceObject(task.getWalkToPosition().getX(), 3552, client.getPosition().getX(), client.getPosition().getY(), Objects.requireNonNull(object).getSizeX(), object.getSizeY(), client.getPosition().getZ());
                if(objectID == 16466)
                    objectPosition = Misc.goodDistanceObject(task.getWalkToPosition().getX(), 2972, client.getPosition().getX(), client.getPosition().getY(), 1, 1, client.getPosition().getZ());
                if(objectID == 11643) {
                    objectPosition = Misc.goodDistanceObject(task.getWalkToPosition().getX(), task.getWalkToPosition().getY(), client.getPosition().getX(), client.getPosition().getY(), 2, client.getPosition().getZ());
                }
                if (objectPosition == null)
                    this.stop();
                atObject(client, task.getWalkToId(), task.getWalkToPosition(), object);
                client.setWalkToTask(null);
                this.stop();
            }
        });
    }

    public void atObject(Client client, int objectID, Position objectPosition, GameObjectData obj) {
        String objectName = obj == null ? "" : obj.getName().toLowerCase();
        if (!client.validClient || client.randomed) {
            return;
        }
        Position pos = client.getPosition().copy();
        int xDiff = Math.abs(pos.getX() - objectPosition.getX());
        int yDiff = Math.abs(pos.getY() - objectPosition.getY());
        if (client.adding) {
            client.objects.add(new RS2Object(objectID, objectPosition.getX(), objectPosition.getY(), 1));
        }
        if (System.currentTimeMillis() < client.walkBlock || client.genie || client.antique) {
            return;
        }
        client.resetAction(false);
        client.setFocus(objectPosition.getX(), objectPosition.getY());
        if (xDiff > 5 || yDiff > 5) {
            return;
        }
        if (Balloons.lootBalloon(client, objectPosition.copy()) && objectID >= 115 && objectID <= 122) {
            return;
        }
        client.farming.interactBin(client, objectID, 1);
        client.farming.clickPatch(client, objectID);
        if (objectID == 26193) {
            Balloons.openInterface(client);
            return;
        }
        if (objectID == 26194 && client.playerRights > 1) {
            Balloons.triggerPartyEvent(client);
            return;
        }
        if (objectID == 23271) {
            client.transport(new Position(objectPosition.getX(), objectPosition.getY() + (client.getPosition().getY() == 3523 ? -1 : 2), objectPosition.getZ()));
        }
        if ((objectID == 6451 && client.getPosition().getY() == 9375) || (objectID == 6452 && client.getPosition().getY() == 9376)) {
            if (client.getPosition().getX() == 3305) {
                Agility agi = new Agility(client);
                agi.kbdEntrance();
            } else
                client.NpcDialogue = 536;
            return;
        }
        if (objectID == 20873) {
            Thieving.attemptSteal(client, objectID, objectPosition);
        }
        if (objectID == 2391 || objectID == 2392) {
            if (client.premium) {
                client.ReplaceObject(2728, 3349, 2391, 0, 0);
                client.ReplaceObject(2729, 3349, 2392, -2, 0);
            }
            return;
        }
        if (objectID == 2097) {
            int type = -1;
            int[] possibleBars = {2349, 2351, 2353, 2359, 2361, 2363};
            for (int possibleBar : possibleBars)
                if (client.contains(possibleBar))
                    type = possibleBar;
            if (type != -1 && client.CheckSmithing(type) != -1) {
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.OpenSmithingFrame(client.CheckSmithing(type));
            } else if (type == -1)
                client.send(new SendMessage("You do not have any bars to smith!"));
        }
        if (objectID == 1294) {
            client.transport(new Position(2485, 9912, 0));
        }
        if (objectID == 17384 && objectPosition.getX() == 2892 && objectPosition.getY() == 3507) {
            client.transport(new Position(2893, 9907, 0));
        }
        if (objectID == 17384 && objectPosition.getX() == 2677 && objectPosition.getY() == 3405) {
            client.transport(new Position(2677, 9806, 0));
        }
        if (objectID == 17385 && objectPosition.getX() == 2677 && objectPosition.getY() == 9805) {
            client.transport(new Position(2677, 3404, 0));
        }
        if (objectID == 17387 && objectPosition.getX() == 2892 && objectPosition.getY() == 9907) {
            client.transport(new Position(2893, 3507, 0));
        }
        if (objectID == 20877 && objectPosition.getX() == 2743 && objectPosition.getY() == 3153) {
            if (!client.checkUnlock(0) && client.checkUnlockPaid(0) != 1) {
                client.showNPCChat(2345, 596, new String[]{"You have not paid yet to enter my dungeon."});
                return;
            }
            client.addUnlocks(0, "0", client.checkUnlock(0) ? "1" : "0");
            client.showNPCChat(2345, 592, new String[]{"Welcome to my dungeon."});
            client.transport(new Position(3748, 9373 + Misc.random(1), 0));
        }
        if (objectID == 5553 && objectPosition.getX() == 3749 && objectPosition.getY() == 9373) {
            client.showNPCChat(2345, 593, new String[]{"Welcome back out from my dungeon."});
            client.transport(new Position(2744 + Misc.random(1), 3153, 0));
        }
        if (objectID == 6702 && objectPosition.getX() == 3749 && objectPosition.getY() == 9374) {
            client.showNPCChat(2345, 593, new String[]{"Welcome back out from my dungeon."});
            client.transport(new Position(2744 + Misc.random(1), 3153, 0));
        }
        if (objectID == 14914) {
            if (!client.checkUnlock(1) && client.checkUnlockPaid(1) != 1) {
                client.showNPCChat(2180, 596, new String[]{"You have not paid yet to enter my cave."});
                return;
            }
            client.addUnlocks(1, "0", client.checkUnlock(1) ? "1" : "0");
            client.showNPCChat(2180, 592, new String[]{"Welcome to my cave."});
            client.transport(new Position(2444, 5169, 0));
            client.GetBonus(true);
        }
        if (objectID == 2352 && objectPosition.getX() == 2443 && objectPosition.getY() == 5169) {
            client.showNPCChat(2180, 593, new String[]{"Welcome back out from my cave."});
            client.transport(new Position(2848, 2991, 0));
            client.GetBonus(true);
        }
        if (objectID == 16466) {
            if (client.getLevel(Skill.AGILITY) < 75) {
                client.send(new SendMessage("You need level 75 agility to use this shortcut!"));
                return;
            }
            client.transport(new Position(2863, client.getPosition().getY() == 2971 ? 2976 : 2971, 0));
        }
        if (objectID == 882 && objectPosition.getX() == 2899 && objectPosition.getY() == 9728) {
            if (client.getLevel(Skill.AGILITY) < 85) {
                client.send(new SendMessage("You need level 85 agility to use this shortcut!"));
                return;
            }
            client.transport(new Position(2885, 9795, 0));
        }
        if (objectID == 882 && objectPosition.getX() == 2885 && objectPosition.getY() == 9794) {
            if (client.getLevel(Skill.AGILITY) < 85) {
                client.send(new SendMessage("You need level 85 agility to use this shortcut!"));
                return;
            }
            client.transport(new Position(2899, 9729, 0));
        }
        if (objectID == 16509) {
            if (!client.checkItem(989) || client.getLevel(Skill.AGILITY) < 70) {
                client.send(new SendMessage("You need a crystal key and 70 agility to use this shortcut!"));
                return;
            }
            if (client.getPosition().getX() == 2886 && client.getPosition().getY() == 9799)
                client.transport(new Position(2892, 9799, 0));
            else if (client.getPosition().getX() == 2892 && client.getPosition().getY() == 9799)
                client.transport(new Position(2886, 9799, 0));
        }
        if (objectID == 16510) {
            if (!client.checkItem(989) || client.getLevel(Skill.AGILITY) < 70) {
                client.send(new SendMessage("You need a crystal key and 70 agility to use this shortcut!"));
                return;
            }
            if (client.getPosition().getX() == 2880 && client.getPosition().getY() == 9813)
                client.transport(new Position(2878, 9813, 0));
            else if (client.getPosition().getX() == 2878 && client.getPosition().getY() == 9813)
                client.transport(new Position(2880, 9813, 0));
        }
        if (objectID == 6847) {
            Thieving.attemptSteal(client, objectID, objectPosition);
            // 	client.addItem(4084, 1);
        }
        if (objectID == 133) { // new dragon teleport?
            client.send(new SendMessage("Welcome to the dragon lair!"));
            client.transport(new Position(3235, 9366, 0));
        }
        if (objectID == 3994 || objectID == 11666 || objectID == 16469 || objectID == 29662) {
            for (int fi = 0; fi < Utils.smelt_frame.length; fi++) {
                client.sendFrame246(Utils.smelt_frame[fi], 150, Utils.smelt_bars[fi][0]);
            }
            client.sendFrame164(2400);
        }
        if (objectID == 2309 && objectPosition.getX() == 2998 && objectPosition.getY() == 3917) {
            if (client.getLevel(Skill.AGILITY) < 75) {
                client.send(new SendMessage("You need at least 75 agility to enter!"));
                return;
            }
            client.ReplaceObject(2998, 3917, 2309, 2, 0);
            return;
        }
        if (objectID == 2624 || objectID == 2625) { //Heroes dungeon for runite rock.
            client.ReplaceObject(2901, 3510, 2624, -1, 0);
            client.ReplaceObject(2901, 3511, 2625, -3, 0);
            client.ReplaceObject(2902, 3510, -1, -1, 0);
            client.ReplaceObject(2902, 3511, -1, -3, 0);
            return;
        }
        if (objectID == 11635) {
            client.transport(new Position(3543, 3463, 0));
            return;
        }
        if ((objectID == 1524 || objectID == 1521) && (objectPosition.getX() == 2908 || objectPosition.getX() == 2907) && objectPosition.getY() == 9698) {
            if (!client.checkItem(989)) {
                client.send(new SendMessage("You need a crystal key to open this door."));
                return;
            }
            if (client.getLevel(Skill.SLAYER) < 120) {
                client.send(new SendMessage("You need at least 120 slayer to enter!"));
                return;
            }
            client.ReplaceObject(2908, 9698, -1, 0, 0);
            client.ReplaceObject(2907, 9698, -1, 0, 0);
            client.ReplaceObject(2908, 9697, 1516, 2, 0);
            client.ReplaceObject(2907, 9697, 1516, 0, 0);
            return;
        }
        if (objectID == 2623) {
            if (client.checkItem(989)) {
                client.ReplaceObject(2924, 9803, 2623, -3, 0);
            } else {
                client.send(new SendMessage("You need the crystal key to enter"));
                client.send(new SendMessage("The crystal key is made from 2 crystal pieces"));
            }
        }
        if (objectID == 16680 && objectPosition.getX() == 2884 && objectPosition.getY() == 3397) {
            if (client.getLevel(Skill.SLAYER) < 50) {
                client.send(new SendMessage("You need at least level 50 slayer to enter the Taverly Dungeon."));
                return;
            }
            client.transport(new Position(2884, 9798, 0));
        }
        if (objectID == 17385 && objectPosition.getX() == 2884 && objectPosition.getY() == 9797) {
            client.transport(new Position(2884, 3398, 0));
        }
        if (objectID == 25939 && objectPosition.getX() == 2715 && objectPosition.getY() == 3470) {
            client.transport(new Position(2715, 3471, 0));
        }
        if (objectID == 25938 && objectPosition.getX() == 2715 && objectPosition.getY() == 3470) {
            client.transport(new Position(2714, 3470, 1));
        }
        if (objectID == 16683 && objectPosition.getX() == 2597 && objectPosition.getY() == 3107) {
            client.transport(new Position(2597, 3106, 1));
        }
        if (objectID == 16681 && objectPosition.getX() == 2597 && objectPosition.getY() == 3107) {
            client.transport(new Position(2597, 3106, 0));
        }
        if (objectID == 410 && objectPosition.getX() == 2925 && objectPosition.getY() == 3483) { //Guthix altar to cosmic
            client.requestAnim(645, 0);
            int random = Misc.random(3);
            switch(random) {
                case 1: //East
                    client.transport(new Position(2162 + Misc.random(2), 4831 + Misc.random(4), 0));
                    break;
                case 2: //South
                    client.transport(new Position(2140 + Misc.random(4), 4811 + Misc.random(2), 0));
                    break;
                case 3: //West
                    client.transport(new Position(2120 + Misc.random(2), 4831 + Misc.random(4), 0));
                    break;
                default: //North
                    client.transport(new Position(2140 + Misc.random(4), 4853 + Misc.random(2), 0));
            }
            return;
        }
        if (objectID == 14847) {
            client.requestAnim(645, 0);
            client.transport(new Position(2924, 3483, 0));
            return;
        }
        if (objectID == 1725) {
            client.stairs = "legendsUp".hashCode();
            client.skillX = objectPosition.getX();
            client.setSkillY(objectPosition.getY());
            client.stairDistance = 1;
        }
        if (objectID == 1725 && objectPosition.getX() == 2732 && objectPosition.getY() == 3377) {
            if (Utils.getDistance(client.getPosition().getX(), client.getPosition().getY(), objectPosition.getX(),
                    objectPosition.getY()) > 2) {
                return;
            }
            if (client.premium)
                client.transport(new Position(2732, 3380, 1));
        }
        if (objectID == 1726 && objectPosition.getX() == 2732 && objectPosition.getY() == 3378) {
            if (Utils.getDistance(client.getPosition().getX(), client.getPosition().getY(), objectPosition.getX(),
                    objectPosition.getY()) > 2) {
                return;
            }
            if (client.premium)
                client.transport(new Position(2732, 3376, 0));
        }
        if (objectID == 1726) {
            client.stairs = "legendsDown".hashCode();
            client.skillX = objectPosition.getX();
            client.setSkillY(objectPosition.getY());
            client.stairDistance = 1;
        }
        if (objectID >= 26622 && objectID <= 26625) {
            if(client.getLevel(Skill.THIEVING) < 21 || client.getStunTimer() > 0) {
                client.send(new SendMessage(client.getLevel(Skill.THIEVING) < 21 ? "You need level 21 thieving to enter." : "You are stunned!"));
                return;
            }
            if(Server.entryObject.getEntryDoor(objectPosition)) { //Entry to activity
                int chance = Misc.random(255);
                if(chance <= (int)(client.getLevel(Skill.THIEVING) * 2.5))
                    client.transport(new Position(1934, 4450, 2));
                else {
                    client.dealDamage(null, Misc.random(3), Entity.hitType.STANDARD);
                    client.setStunTimer(4);
                }
            } else
                client.transport(new Position(1968, 4420, 2));
        }
        if (objectID >= 26618 && objectID <= 26621) { //Room entrance check...For now :D
            if(client.getPlunder.getRoomNr() + 1 == 8) { //Max floors!
                return;
            }
            if(Server.entryObject.nextRoom[client.getPlunder.getRoomNr()] + 26618 == objectID && client.getPlunder.openDoor(objectID))
                client.getPlunder.nextRoom();
            else if (client.getPlunder.openDoor(objectID))
                client.send(new SendMessage("This tomb door lead nowhere."));
            else client.getPlunder.toggleObstacles(objectID);
        }
        if (objectID == 20932) {
            client.transport(client.getPlunder.end);
        }
        if (objectID == 20931) {
            client.NpcDialogue = 20931;
        }
        if (objectID == 26616 || objectID == 26626) { //Grand gold chest + Sarcophagus
            client.getPlunder.toggleObstacles(objectID);
        }
        if (objectID == 26580 || (objectID >= 26600 && objectID <= 26613)) { //Urns!
            client.getPlunder.toggleObstacles(objectID);
        }
        /* Desert Objects */
        if (objectID == 20275) { //Sophanem bank entry!
            client.transport(new Position(2799, 5160, 0));
            client.setFocus(2799, 5159);
        }
        else if (objectID == 20277) { //Sophanem bank exit!
            client.transport(new Position(3315, 2796, 0));
            client.setFocus(3315, 2797);
        }
        /* Agility */
        Agility agility = new Agility(client);
        if (objectID == 23145) {
            agility.GnomeLog();
            return;
        } else if (objectID == 23134 && client.distanceToPoint(objectPosition.getX(), objectPosition.getY()) < 2) {
            agility.GnomeNet1();
            return;
        } else if (objectID == 23559) {
            agility.GnomeTree1();
            return;
        } else if (objectID == 23557) {
            agility.GnomeRope();
            return;
        } else if (objectID == 23560 || objectID == 23561) {
            agility.GnomeTreebranch2();
            return;
        } else if (objectID == 23135 && client.distanceToPoint(objectPosition.getX(), objectPosition.getY()) < 3) {
            agility.GnomeNet2();
            return;
        } else if (objectID == 23138 && client.getPosition().getX() == 2484 && client.getPosition().getY() == 3430 && client.distanceToPoint(objectPosition.getX(), objectPosition.getY()) < 2) {
            agility.GnomePipe();
            return;
        } else if (objectID == 23139 && client.getPosition().getX() == 2487 && client.getPosition().getY() == 3430 && client.distanceToPoint(objectPosition.getX(), objectPosition.getY()) < 2) {
            agility.GnomePipe();
            return;
        } else if (objectID == 23137) {
            agility.WildyPipe();
            return;
        } else if (objectID == 23132) {
            agility.WildyRope();
            return;
        } else if (objectID == 23556) {
            agility.WildyStones();
            return;
        } else if (objectID == 23542) {
            agility.WildyLog();
            return;
        } else if (objectID == 23640) {
            agility.WildyClimb();
            return;
        } else if (objectID == 23131) {
            agility.BarbRope();
            return;
        } else if (objectID == 23144) {
            agility.BarbLog();
            return;
        } else if (objectID == 20211) {
            agility.BarbNet();
            return;
        } else if (objectID == 23547) {
            agility.BarbLedge();
            return;
        } else if (objectID == 16682) {
            agility.BarbStairs();
            return;
        } else if (objectID == 1948 && objectPosition.getX() == 2536 && objectPosition.getY() == 3553) {
            agility.BarbFirstWall();
            return;
        } else if (objectID == 1948 && objectPosition.getX() == 2539 && objectPosition.getY() == 3553) {
            agility.BarbSecondWall();
            return;
        } else if (objectID == 1948 && objectPosition.getX() == 2542 && objectPosition.getY() == 3553) {
            agility.BarbFinishWall();
            return;
        } else if (objectID == 23567) {
            agility.orangeBar();
        } else if (objectID == 23548) {
            agility.yellowLedge();
        }
        /* Werewolf course */
        Werewolf werewolf = new Werewolf(client);
        if (objectID == 11643) {
            werewolf.StepStone(objectPosition);
            return;
        }
        if (objectID == 11638) {
            werewolf.hurdle(objectPosition);
            return;
        }
        if (objectID == 11657) {
            werewolf.pipe(objectPosition);
            return;
        }
        if (objectID == 11641) {
            werewolf.slope(objectPosition);
            return;
        }
        if (objectID >= 11644 && objectID <= 11646) {
            werewolf.zipLine(objectPosition);
            return;
        }
        if (objectID == 11636) { //Werewolf entrance
            if (client.getLevel(Skill.AGILITY) >= 60) {
                client.ReplaceObject(objectPosition.getX(), objectPosition.getY(), 11636, 2, 10); //Do we need to showcase the trapdoor being opened?
                client.showNPCChat(5928, 601, new String[]{"Welcome to the werewolf agility course!"});
                client.transport(new Position(3549, 9865, 0));
            } else client.showNPCChat(5928, 616, new String[]{"Go and train your agility!"});
            return;
        }
        /* Something else... */
        if (objectID == 1558 || objectID == 1557 && client.distanceToPoint(2758, 3482) < 5 && client.playerRights > 0) {
            client.ReplaceObject(2758, 3482, 1558, -2, 0);
            client.ReplaceObject(2757, 3482, 1557, 0, 0);
            client.send(new SendMessage("Welcome to the Castle"));
        }
        if (objectID == 2104) {
            objectID = 2105;
        }
        if (objectID == 2102) {
            objectID = 2103;
        }
        /* Woodcutting */
        if (client.CheckObjectSkill(objectID, objectName)) {
            if (client.fletchings || client.isFiremaking || client.shafting) { //We need this here?!
                client.resetAction();
            }
            if (client.cuttingIndex < 0) {
                client.resetAction();
                return;
            }
            int WCAxe = client.findAxe();
            if (WCAxe < 0) {
                client.send(new SendMessage("You need an axe in which you got the required woodcutting level for."));
                client.resetAction();
                return;
            }
            int level = Utils.woodcuttingLevels[client.cuttingIndex];
            if (level > client.getLevel(Skill.WOODCUTTING)) {
                client.send(new SendMessage(
                        "You need a woodcutting level of " + level + " to cut this tree."));
                client.resetAction();
                return;
            }
            if (client.freeSlots() < 1) {
                client.send(new SendMessage("You got full inventory!"));
                client.resetAction();
                return;
            }
            client.resourcesGathered = 0;
            client.lastAxeAction = System.currentTimeMillis() + 600;
            client.lastAction = System.currentTimeMillis() - 600;
            client.woodcutting = true;
            client.requestAnim(client.getWoodcuttingEmote(Utils.axes[WCAxe]), 0);
            client.send(new SendMessage("You swing your axe at the tree..."));
            return;
        }
        /* Mining */
        int rockId = -1;
        for (int r = 0; r < Utils.rocks.length && rockId == -1; r++) //Check rock object!
            if (objectID == Utils.rocks[r]) rockId = r;

        if(rockId != -1) {
            if (client.fletchings || client.isFiremaking || client.shafting) { //We need this here?!
                client.resetAction();
            }
            if (client.getPositionName(client.getPosition()) == Client.positions.TZHAAR) {
                client.send(new SendMessage("You can not mine here or the Tzhaar's will be angry!"));
                return;
            }
            if (client.getLevel(Skill.MINING) < Utils.rockLevels[rockId]) {
                client.send(new SendMessage("You need a mining level of " + Utils.rockLevels[rockId] + " to mine this rock"));
                return;
            }
            int minePick = client.findPick();
            if (minePick == -1) {
                client.resetAction();
                client.send(new SendMessage("You need a pickaxe in which you got the required mining level for."));
                return;
            }
            client.resourcesGathered = 0;
            client.lastPickAction = System.currentTimeMillis() + 600;
            client.lastAction = System.currentTimeMillis() - 600;
            client.mineIndex = rockId; //Rock id need to be here!
            client.mining = true;
            client.requestAnim(client.getMiningEmote(Utils.picks[minePick]), 0);
            client.send(new SendMessage("You swing your pick at the rock..."));
        }
        if (objectID == 2634 && objectPosition.getX() == 2838 && objectPosition.getY() == 3517) { //2838, 3517
            client.send(new SendMessage("You jump to the other side of the rubble"));
            client.transport(new Position(2840, 3517, 0));
        }
        /* Unsure... */
        if (objectID == 16680) {
            int[] x = {2845, 2848, 2848};
            int[] y = {3516, 3513, 3519};
            for (int c = 0; c < x.length; c++) {
                if (objectPosition.getX() == x[c] && objectPosition.getY() == y[c]) {
                    client.transport(new Position(2868, 9945, 0));
                    c = x.length;
                }
            }
        }
        if (objectID == 2492) {
            client.transport(new Position(2591, 3087, 0));
            return;
        }
        if (objectID == 14905) {
            client.runecraft(561, 1, 60);
            return;
        }
        if (objectID == 27978) {
            client.runecraft(565, 50, 85);
            return;
        }
        if (objectID == 14903) {
            client.runecraft(564, 75, 120);
            return;
        }
        if (objectID == 2158 || objectID == 2156) {
            client.triggerTele(2921, 4844, 0, false);
            return;
        }
        if (objectID == 733) {
            int chance = Misc.chance(100);
            if (chance <= 50) {
                client.send(new SendMessage("You failed to cut the web!"));
                return;
            }
            if (System.currentTimeMillis() - client.lastAction < 2000) {
                client.lastAction = System.currentTimeMillis();
                return;
            }
            final Object emptyObj = new Object(734, objectPosition.getX(), objectPosition.getY(), client.getPosition().getZ(), 10, 1, objectID);
            if (!GlobalObject.addGlobalObject(emptyObj, 30000)) {
                return;
            }
            client.lastAction = System.currentTimeMillis();
            return;
        }
        if (objectID == 16520 || objectID == 16519) {
            if (client.getLevel(Skill.AGILITY) < 50) {
                client.send(new SendMessage("You need level 50 agility to use this shortcut!"));
                return;
            }
            if (objectPosition.getX() == 2575 && objectPosition.getY() == 3108)
                client.transport(new Position(2575, 3112, 0));
            else if (objectPosition.getX() == 2575 && objectPosition.getY() == 3111)
                client.transport(new Position(2575, 3107, 0));
            return;
        }
        if (objectID == 16664 && objectPosition.getX() == 2724 && objectPosition.getY() == 3374) {
            if (!client.premium) {
                client.resetPos();
            }
            client.transport(new Position(2727, 9774, 0));
            return;
        }
        if (objectID == 2833) {
            if (objectPosition.getX() == 2544 && objectPosition.getY() == 3111) {
                client.transport(new Position(2544, 3112, 1));
            }
            return;
        }
        if (objectID == 12260) { //Teleport to Kalphite King?
            if (objectPosition.getX() == 2459 && objectPosition.getY() == 4354)
                client.transport(new Position(2941, 4691, 0));
            else client.transport(new Position(2462, 4359, 0));
            return;
        }
        if (objectID == 17122) {
            if (objectPosition.getX() == 2544 && objectPosition.getY() == 3111) {
                client.transport(new Position(2544, 3112, 0));
            }
            return;
        }
        if (objectID == 2796) {
            if (objectPosition.getX() == 2549 && objectPosition.getY() == 3111) {
                client.transport(new Position(2549, 3112, 2));
            }
            return;
        }
        if (objectID == 2797) {
            if (objectPosition.getX() == 2549 && objectPosition.getY() == 3111) {
                client.transport(new Position(2549, 3112, 1));
            }
            return;
        }
        if (objectID == 17384) {
            if (objectPosition.getX() == 2594 && objectPosition.getY() == 3085) {
                client.transport(new Position(2594, 9486, 0));
            }
            return;
        }
        if (objectID == 17385) {
            if (objectPosition.getX() == 2594 && objectPosition.getY() == 9485) {
                client.transport(new Position(2594, 3086, 0));
            }
            return;
        }
        if (objectID == 16665) {
            if (objectPosition.getX() == 2724 && objectPosition.getY() == 9774) {
                if (!client.premium) {
                    client.resetPos();
                }
                client.transport(new Position(2723, 3375, 0));
            } else if (objectPosition.getX() == 2603 && objectPosition.getY() == 9478) {
                client.transport(new Position(2606, 3079, 0));
            } else if (objectPosition.getX() == 2569 && objectPosition.getY() == 9522) {
                client.transport(new Position(2570, 3121, 0));
            }
            return;
        }

//    if (objectID == 377 && client.playerHasItem(605)) {
//      {
//
//        double roll = Math.random() * 100;
//        if (roll < 100) {
//          int[] items = { 4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730, 4732, 4734, 4736,
//              4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759 };
//          int r = (int) (Math.random() * items.length);
//          client.send(new SendMessage("You have recieved a " + client.GetItemName(items[r]) + "!"));
//          client.addItem(items[r], 1);
//          client.deleteItem(605, 1);
//          client.yell("[Server] - " + client.getPlayerName() + " has just received from the BKT chest a  "
//              + client.GetItemName(items[r]));
//        } else {
//          int coins = Utils.random(7000);
//          client.send(new SendMessage("You find " + coins + " coins inside the BKT chest"));
//          client.addItem(995, coins);
//        }
//
//      }
//      for (int p = 0; p < Constants.maxPlayers; p++) {
//        Client player = (Client) PlayerHandler.players[p];
//        if (player == null) {
//          continue;
//        }
//        if (player.getPlayerName() != null && player.getPosition().getZ() == client.getPosition().getZ()
//            && !player.disconnected && Math.abs(player.getPosition().getY() - client.getPosition().getY()) < 30
//            && Math.abs(player.getPosition().getX() - client.getPosition().getX()) < 30 && player.dbId > 0) {
//          player.stillgfx(444, objectPosition.getY(), objectPosition.getX());
//        }
//      }
//    }
        if (objectID == 375 && objectPosition.getX() == 2593 && objectPosition.getY() == 3108 && client.getPosition().getZ() == 1) {
            if(client.chestEventOccur) {
                return;
            }
            if (client.getLevel(Skill.THIEVING) < 70) {
                client.send(new SendMessage("You must be level 70 thieving to open this chest"));
                return;
            }
            if (client.freeSlots() < 1) {
                client.send(new SendMessage("You need atleast one free inventory slot!"));
                return;
            }
            if (System.currentTimeMillis() - client.lastAction < 1200) {
                client.lastAction = System.currentTimeMillis();
                return;
            }
            final Object emptyObj = new Object(378, objectPosition.getX(), objectPosition.getY(), client.getPosition().getZ(), 10, 2, objectID);
            if (!GlobalObject.addGlobalObject(emptyObj, 12000)) {
                return;
            }
            client.lastAction = System.currentTimeMillis();
            double roll = Math.random() * 100;
            if (roll <= 0.3) {
                int[] items = {2577, 2579, 2631};
                int r = (int) (Math.random() * items.length);
                client.send(new SendMessage("You have recieved a " + client.GetItemName(items[r]) + "!"));
                client.addItem(items[r], 1);
                ItemLog.playerGathering(client, items[r], 1, client.getPosition().copy(), "Thieving");
                client.yell("[Server] - " + client.getPlayerName() + " has just received from the Yanille chest a  "
                        + client.GetItemName(items[r]));
            } else {
                int coins = 300 + Utils.random(1200);
                client.send(new SendMessage("You find " + coins + " coins inside the chest"));
                client.addItem(995, coins);
                ItemLog.playerGathering(client, 995, coins, client.getPosition().copy(), "Thieving");
            }
            if (client.getEquipment()[Equipment.Slot.HEAD.getId()] == 2631)
                client.giveExperience(300, Skill.THIEVING);
            client.checkItemUpdate();
            client.chestEvent++;
            client.stillgfx(444, objectPosition.getY(), objectPosition.getX());
            client.triggerRandom(900);
        }
        if (objectID == 375 && objectPosition.getX() == 2733 && objectPosition.getY() == 3374) {
            if(client.chestEventOccur) {
                return;
            }
            if (!client.premium) {
                client.resetPos();
                return;
            }
            if (client.getLevel(Skill.THIEVING) < 85) {
                client.send(new SendMessage("You must be level 85 thieving to open this chest"));
                return;
            }
            if (client.freeSlots() < 1) {
                client.send(new SendMessage("You need atleast one free inventory slot!"));
                return;
            }
            if (System.currentTimeMillis() - client.lastAction < 1200) {
                client.lastAction = System.currentTimeMillis();
                return;
            }
            final Object o = new Object(378, objectPosition.getX(), objectPosition.getY(), objectPosition.getZ(), 11, -1, objectID);
            if (!GlobalObject.addGlobalObject(o, 15000)) {
                return;
            }
            client.lastAction = System.currentTimeMillis();
            double roll = Math.random() * 100;
            if (roll <= 0.3) {
                int[] items = {1050, 2581, 2631};
                int r = (int) (Math.random() * items.length);
                client.send(new SendMessage("You have recieved a " + client.GetItemName(items[r]) + "!"));
                client.addItem(items[r], 1);
                ItemLog.playerGathering(client, items[r], 1, client.getPosition().copy(), "Thieving");
                client.yell("[Server] - " + client.getPlayerName() + " has just received from the Legends chest a  "
                        + client.GetItemName(items[r]));
            } else {
                int coins = 500 + Utils.random(2000);
                client.send(new SendMessage("You find " + coins + " coins inside the chest"));
                client.addItem(995, coins);
                ItemLog.playerGathering(client, 995, coins, client.getPosition().copy(), "Thieving");
            }
            if (client.getEquipment()[Equipment.Slot.HEAD.getId()] == 2631)
                client.giveExperience(500, Skill.THIEVING);
            client.checkItemUpdate();
            client.chestEvent++;
            client.stillgfx(444, objectPosition.getY(), objectPosition.getX());
            client.triggerRandom(1500);
        }
        if (System.currentTimeMillis() - client.lastDoor > 1000) {
            client.lastDoor = System.currentTimeMillis();
            for (int d = 0; d < DoorHandler.doorX.length; d++) {
                if (objectID == DoorHandler.doorId[d] && objectPosition.getX() == DoorHandler.doorX[d]
                        && objectPosition.getY() == DoorHandler.doorY[d]) {
                    int newFace;
                    if (DoorHandler.doorState[d] == 0) { // closed
                        newFace = DoorHandler.doorFaceOpen[d];
                        DoorHandler.doorState[d] = 1;
                    } else {
                        newFace = DoorHandler.doorFaceClosed[d];
                        DoorHandler.doorState[d] = 0;
                    }
                    DoorHandler.doorFace[d] = newFace;
                    //client.send(new Sound(326));
                    for (int p = 0; p < Constants.maxPlayers; p++) {
                        Client player = (Client) PlayerHandler.players[p];
                        if (player == null) {
                            continue;
                        }
                        if (player.getPlayerName() != null && player.getPosition().getZ() == client.getPosition().getZ()
                                && !player.disconnected && player.getPosition().getY() > 0 && player.getPosition().getX() > 0
                                && player.dbId > 0) {
                            player.ReplaceObject(DoorHandler.doorX[d], DoorHandler.doorY[d], DoorHandler.doorId[d], newFace, 0);
                        }
                    }
                }
            }
        }
        if (objectID == 23140) {
            if (!client.checkItem(1544)) {
                client.send(new SendMessage("You need a orange key to use this pipe!"));
                return;
            }
            if (objectPosition.getX() == 2576 && objectPosition.getY() == 9506)
                client.transport(new Position(2572, 9506, 0));
            else if (objectPosition.getX() == 2573 && objectPosition.getY() == 9506)
                client.transport(new Position(2578, 9506, 0));
        }
        if (objectID == 23564) {
            client.transport(new Position(2621, 9496, 0));
        }
        if (objectID == 15656) {
            client.transport(new Position(2614, 9505, 0));
        }
        if(objectID == 409 || objectID == 20377) {
            if(client.getCurrentPrayer() < client.getMaxPrayer()) {
                client.pray(client.getMaxPrayer());
                client.send(new SendMessage("You restore your prayer points!"));
            } else client.send(new SendMessage("You are at maximum prayer points!"));
        }
        //if (objectID == 6836) {
        //  client.skillX = objectPosition.getX();
        //  client.setSkillY(objectPosition.getY());
        //  client.WanneThieve = 6836;
        // }
        if (objectID == 881) {
            client.getPosition().setZ(client.getPosition().getZ() - 1);
        }
        if (objectID == 1591 && objectPosition.getX() == 3268 && objectPosition.getY() == 3435) {
            if (client.determineCombatLevel() >= 80) {
                client.transport(new Position(2540, 4716, 0));
            } else {
                client.send(new SendMessage("You need to be level 80 or above to enter the mage arena."));
                client.send(new SendMessage("The skeletons at the varrock castle are a good place until then."));
            }
        }
        if (objectID == 5960 && objectPosition.getX() == 2539 && objectPosition.getY() == 4712) {
            client.transport(new Position(3105, 3933, 0));
        }

        // Wo0t Tzhaar Objects

        if (objectID == 9369 && (objectPosition.getX() == 2399) && (objectPosition.getY() == 5176)) {
            if (client.getPosition().getY() == 5177) {
                client.transport(new Position(2399, 5175, 0));
            }
        }
        if (objectID == 9369 && (objectPosition.getX() == 2399) && (objectPosition.getY() == 5176)) {
            if (client.getPosition().getY() == 5175) {
                client.transport(new Position(2399, 5177, 0));
            }
        }

        if (objectID == 9368 && (objectPosition.getX() == 2399) && (objectPosition.getY() == 5168)) {
            if (client.getPosition().getY() == 5169) {
                client.transport(new Position(2399, 5167, 0));
            }
        }
        if (objectID == 9391 && (objectPosition.getX() == 2399) && (objectPosition.getY() == 5172)) // Tzhaar bank?
        {
            client.openUpBank();
        }
        if (objectName.toLowerCase().startsWith("bank") || objectName.toLowerCase().contains("bank"))
            client.openUpBank();
        if (objectID == 11833 && objectPosition.getX() == 2437 && objectPosition.getY() == 5166) // Jad entrance
        {
            client.send(new SendMessage("You have entered the Jad Cave."));
            client.transport(new Position(2413, 5117, 0));
        }
        if (objectID == 11834 && objectPosition.getX() == 2412 && objectPosition.getY() == 5118) // Jad exit
        {
            client.send(new SendMessage("You have left the Jad Cave."));
            client.transport(new Position(2438, 5168, 0));
        }
        // End of Tzhaar Objects

        if ((objectID == 2213) || (objectID == 2214) || (objectID == 3045) || (objectID == 5276)
                || (objectID == 6084)) {
            //System.out.println("Banking..");
            client.skillX = objectPosition.getX();
            client.setSkillY(objectPosition.getY());
            client.WanneBank = 1;
            client.WanneShop = -1;
        }
        /* Gnome Village stairs! */
        if (objectID == 16675 && objectPosition.getX() == 2488 && objectPosition.getY() == 3407) // spinning wheel stairs 1
        {
            client.transport(new Position(2489, 3409, 1));
        }
        if (objectID == 16677 && objectPosition.getX() == 2489 && objectPosition.getY() == 3408) // spinning wheel stairs 1
        {
            client.transport(new Position(2488, 3406, 0));
        }
        if (objectID == 16675 && objectPosition.getX() == 2485 && objectPosition.getY() == 3402) // spinning wheel stairs 2
        {
            client.transport(new Position(2485, 3401, 1));
        }
        if (objectID == 16677 && objectPosition.getX() == 2485 && objectPosition.getY() == 3402) // spinning wheel stairs 2
        {
            client.transport(new Position(2485, 3404, 0));
        }

        if (objectID == 16675 && objectPosition.getX() == 2445 && objectPosition.getY() == 3434) // Bank staircase 1
        {
            client.transport(new Position(2445, 3433, 1));
        }
        if (objectID == 16677 && objectPosition.getX() == 2445 && objectPosition.getY() == 3434) // Bank staircase 1
        {
            client.transport(new Position(2446, 3436, 0));
        }
        if (objectID == 16675 && objectPosition.getX() == 2444 && objectPosition.getY() == 3414) // Bank staircase 2
        {
            client.transport(new Position(2445, 3416, 1));
        }
        if (objectID == 16677 && objectPosition.getX() == 2445 && objectPosition.getY() == 3415) // Bank staircase 2
        {
            client.transport(new Position(2444, 3413, 0));
        }
        // }
        // go upstairs
            if (objectID == 1747) {
                client.stairs = 1;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 1738) {
                client.stairs = 1;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 2;
            } else if (objectID == 1734) {
                client.stairs = 10;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 3;
                client.stairDistanceAdd = 1;
            } else if (objectID == 55) {
                client.stairs = 15;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 3;
                client.stairDistanceAdd = 1;
            } else if (objectID == 57) {
                client.stairs = 15;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 3;
            } else if (objectID == 1755 || objectID == 5946 || objectID == 1757) {
                client.stairs = 4;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 2;
            } else if (objectID == 1764) {
                client.stairs = 12;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 2148) {
                client.stairs = 8;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 3608) {
                client.stairs = 13;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 2408) {
                client.stairs = 16;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 5055) {
                client.stairs = 18;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 5131) {
                client.stairs = 20;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 9359) {
                client.stairs = 24;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 2406) { /* Lost City Door */
                if (client.getEquipment()[Equipment.Slot.WEAPON.getId()] == 772) { // Dramen
                    // Staff
                    client.stairs = 27;
                    client.skillX = objectPosition.getX();
                    client.setSkillY(objectPosition.getY());
                    client.stairDistance = 1;
                }
            }
            // go downstairs
            if (objectID == 1746 || objectID == 1749) {
                client.stairs = 2;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 1740) {
                client.stairs = 2;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 1723) {
                client.stairs = 22;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 2;
                client.stairDistanceAdd = 2;
            } else if (objectID == 16664) {
                if (objectPosition.getX() == 2603 && objectPosition.getY() == 3078) {
                    if (!client.checkItem(1543)) {
                        client.send(new SendMessage("You need a red key to go down these stairs!"));
                        return;
                    }
                    client.transport(new Position(2602, 9479, 0));
                } else if (objectPosition.getX() == 2569 && objectPosition.getY() == 3122) {
                    if (!client.checkItem(1545)) {
                        client.send(new SendMessage("You need a yellow key to use this staircase!"));
                        return;
                    }
                    client.transport(new Position(2570, 9525, 0));
                }
            } else if (objectID == 1992 && objectPosition.getX() == 2558 && objectPosition.getY() == 3444) {
//        if (client.playerHasItem(537, 50)) {
//          client.deleteItem(537, client.getItemSlot(537), 50);
//          client.send(new SendMessage("The gods accept your offer!"));
                client.transport(new Position(2717, 9820, 0));
//        } else {
//          client.send(new SendMessage("The gods require 50 dragon bones as a sacrifice!"));
//          return;
//        }
            } else if (objectID == 54) {
                client.stairs = 14;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 3;
                client.stairDistanceAdd = 1;
            } else if (objectID == 56) {
                client.stairs = 14;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 3;
            } else if (objectID == 1568 || objectID == 5947 || objectID == 6434
                    || /* objectID == 1759 || */objectID == 1570) {
                if (objectPosition.getX() == 2594 && objectPosition.getY() == 3085)
                    return;
                client.stairs = 3;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 2113) { // Mining guild stairs
                if (client.getLevel(Skill.MINING) >= 60) {
                    client.stairs = 3;
                    client.skillX = objectPosition.getX();
                    client.setSkillY(objectPosition.getY());
                    client.stairDistance = 1;
                } else {
                    client.send(new SendMessage("You need 60 mining to enter the mining guild."));
                }
            } else if (objectID == 492) {
                client.stairs = 11;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 2;
            } else if (objectID == 2147) {
                client.stairs = 7;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 5054) {
                client.stairs = 17;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 5130) {
                client.stairs = 19;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 9358) {
                client.stairs = 23;
                client.skillX = objectPosition.getX();
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 5488) {
                client.stairs = 28;
                client.setSkillX(objectPosition.getX());
                client.setSkillY(objectPosition.getY());
                client.stairDistance = 1;
            } else if (objectID == 9294) {
                if (objectPosition.getX() == 2879 && objectPosition.getY() == 9813) {
                    client.stairs = "trap".hashCode();
                    client.stairDistance = 1;
                    client.setSkillX(objectPosition.getX());
                    client.setSkillY(objectPosition.getY());
                }
            }
    }

}