package threewks.contactsync.service.sync;

import org.apache.commons.codec.digest.DigestUtils;

public interface Hasher {
    /**
     * Simple hasher that calls Object#toString() and performs a SHA1 hash on the resulting string.
     */
    Hasher TO_STRING_SHA1_HASHER = object -> DigestUtils.sha1Hex(String.valueOf(object));

    /**
     * Given an object, produce an asymmetric cryptographic hash.
     * @param object the object to hash
     * @return the hash
     */
    String hash(Object object);
}
