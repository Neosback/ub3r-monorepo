package net.dodian.utilities;

// Assuming net.dodian.utilities.Cryption exists and is similar to typical ISAAC implementations.
// If Cryption.java is not found or has a different API, this class will need adjustment.

/**
 * Encapsulates the ISAAC cipher logic, using an underlying Cryption object.
 */
public class IsaacCipher {

    private final Cryption cryption;

    /**
     * Constructs a new IsaacCipher with the given seed.
     *
     * @param seed The seed for the ISAAC cipher. The seed should be an array of 256 integers.
     *             Typically, this seed is derived from a session key exchanged during handshake.
     */
    public IsaacCipher(int[] seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Seed cannot be null.");
        }
        // Assuming Cryption constructor takes the seed directly.
        // And assuming Cryption.getNextKey() exists.
        this.cryption = new Cryption(seed);
    }

    /**
     * Generates the next ISAAC key from the stream.
     * This key is typically XORed with the data to be encrypted/decrypted.
     *
     * @return The next integer key from the ISAAC sequence.
     */
    public int getNextKey() {
        return this.cryption.getNextKey();
    }

    /**
     * Encrypts a single byte.
     *
     * @param data The byte to encrypt.
     * @return The encrypted byte.
     */
    public byte encrypt(byte data) {
        return (byte) (data ^ (getNextKey() & 0xFF));
    }

    /**
     * Decrypts a single byte.
     *
     * @param data The byte to decrypt.
     * @return The decrypted byte.
     */
    public byte decrypt(byte data) {
        return (byte) (data ^ (getNextKey() & 0xFF));
    }
}
