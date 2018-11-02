package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Random;

import static java.util.Arrays.asList;

public class BlobQueriesTest {
    @Test
    public void testSmallBlobs() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS blobbies"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE blobbies(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "tiny_one TINYBLOB," +
                    "blob_one BLOB," +
                    "medium_one MEDIUMBLOB," +
                    "long_one LONGBLOB" +
                ")"
            ));

            byte[] tinyBytes = randomBytes(177);
            byte[] blobBytes = randomBytes(5187);
            byte[] mediumBytes = randomBytes(12110);
            byte[] longBytes = randomBytes(155151);

            byte[] tinyBytes2 = randomBytes(157);
            byte[] blobBytes2 = randomBytes(5117);
            byte[] mediumBytes2 = randomBytes(16666);
            byte[] longBytes2 = randomBytes(298765);
            // (those lengths don't have any special meaning)

            Object[][] expectBlobs = {
                { 1, tinyBytes,  null,       null,         null       },
                { 2, null,       blobBytes,  null,         null       },
                { 3, null,       null,       mediumBytes,  null       },
                { 4, null,       null,       null,         longBytes  },
                { 5, tinyBytes2, blobBytes2, mediumBytes2, longBytes2 }
            };

            // we'll also use hashes to verify the server agrees on the content (e.g. if we
            // accidentally XORed all the bytes in both directions with some value, it'd
            // look the same here, but not in the database)
            Object[][] expectHashes = {
                { 1, sha1(tinyBytes),  null,             null,               null             },
                { 2, null,             sha1(blobBytes),  null,               null             },
                { 3, null,             null,             sha1(mediumBytes),  null             },
                { 4, null,             null,             null,               sha1(longBytes)  },
                { 5, sha1(tinyBytes2), sha1(blobBytes2), sha1(mediumBytes2), sha1(longBytes2) }
            };

            helper.expectSuccess(conn.sendQuery("INSERT INTO blobbies VALUES(?, ?, ?, ?, ?)", asList(expectBlobs[0])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO blobbies VALUES(?, ?, ?, ?, ?)", asList(expectBlobs[1])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO blobbies VALUES(?, ?, ?, ?, ?)", asList(expectBlobs[2])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO blobbies VALUES(?, ?, ?, ?, ?)", asList(expectBlobs[3])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO blobbies VALUES(?, ?, ?, ?, ?)", asList(expectBlobs[4])));

            helper.expectResultSetValues(conn.sendQuery("SELECT id, sha1(tiny_one), sha1(blob_one), sha1(medium_one), sha1(long_one) FROM blobbies ORDER BY id"), expectHashes);
            helper.expectResultSetValues(conn.sendQuery("SELECT id, tiny_one, blob_one, medium_one, long_one FROM blobbies ORDER BY id"), expectBlobs);

            helper.expectSuccess(conn.sendQuery("DROP TABLE blobbies"));

            helper.expectSuccess(conn.close());

            testFinished.complete(null);
        });
    }

    @Test
    public void testSmallBlobsWithPS() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS blobbies_ps"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE blobbies_ps(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "tiny_one TINYBLOB," +
                    "blob_one BLOB," +
                    "medium_one MEDIUMBLOB," +
                    "long_one LONGBLOB" +
                ")"
            ));

            byte[] tinyBytes = randomBytes(177);
            byte[] blobBytes = randomBytes(5187);
            byte[] mediumBytes = randomBytes(12110);
            byte[] longBytes = randomBytes(155151);

            byte[] tinyBytes2 = randomBytes(157);
            byte[] blobBytes2 = randomBytes(5117);
            byte[] mediumBytes2 = randomBytes(16666);
            byte[] longBytes2 = randomBytes(298765);

            Object[][] expectBlobs = {
                { 1, tinyBytes,  null,       null,         null       },
                { 2, null,       blobBytes,  null,         null       },
                { 3, null,       null,       mediumBytes,  null       },
                { 4, null,       null,       null,         longBytes  },
                { 5, tinyBytes2, blobBytes2, mediumBytes2, longBytes2 }
            };

            Object[][] expectHashes = {
                { 1, sha1(tinyBytes),  null,             null,               null             },
                { 2, null,             sha1(blobBytes),  null,               null             },
                { 3, null,             null,             sha1(mediumBytes),  null             },
                { 4, null,             null,             null,               sha1(longBytes)  },
                { 5, sha1(tinyBytes2), sha1(blobBytes2), sha1(mediumBytes2), sha1(longBytes2) }
            };

            helper.expectSuccess(conn.prepareStatement("INSERT INTO blobbies_ps VALUES(?, ?, ?, ?, ?)"), ps -> {
                helper.expectSuccess(ps.execute(asList(expectBlobs[0])));
                helper.expectSuccess(ps.execute(asList(expectBlobs[1])));
                helper.expectSuccess(ps.execute(asList(expectBlobs[2])));
                helper.expectSuccess(ps.execute(asList(expectBlobs[3])));
                helper.expectSuccess(ps.execute(asList(expectBlobs[4])));

                helper.expectSuccess(conn.prepareStatement("SELECT id, sha1(tiny_one), sha1(blob_one), sha1(medium_one), sha1(long_one) FROM blobbies_ps ORDER BY id"), selectShaPs -> {
                    helper.expectResultSetValues(selectShaPs.execute(Collections.emptyList()), expectHashes);
                    helper.expectSuccess(selectShaPs.close());
                });

                helper.expectSuccess(conn.prepareStatement("SELECT id, tiny_one, blob_one, medium_one, long_one FROM blobbies_ps ORDER BY id"), selectPs -> {
                    helper.expectResultSetValues(selectPs.execute(Collections.emptyList()), expectBlobs);
                    helper.expectSuccess(selectPs.close());

                    helper.expectSuccess(conn.sendQuery("DROP TABLE blobbies_ps"));

                    helper.expectSuccess(ps.close());

                    helper.expectSuccess(conn.close());

                    testFinished.complete(null);
                });
            });
        });
    }

    @Test
    public void testLargeBlobs() {
        TestHelper.runTest(60000, (conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS bloobs"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE bloobs(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "medium_one MEDIUMBLOB," +
                    "long_one LONGBLOB" +
                ")"
            ));

            byte[] mediumBytes = randomBytes(15777412);
            byte[] longBytes = randomBytes(35000000);

            byte[] mediumBytes2 = randomBytes(13521412);
            byte[] longBytes2 = randomBytes(18930255);

            Object[][] expectBlobs = {
                { 1, mediumBytes,  null       },
                { 2, null,         longBytes  },
                { 3, mediumBytes2, longBytes2 }
            };

            Object[][] expectHashes = {
                { 1, sha1(mediumBytes),  null             },
                { 2, null,               sha1(longBytes)  },
                { 3, sha1(mediumBytes2), sha1(longBytes2) }
            };

            helper.expectSuccess(conn.sendQuery("INSERT INTO bloobs VALUES(?, ?, ?)", asList(expectBlobs[0])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO bloobs VALUES(?, ?, ?)", asList(expectBlobs[1])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO bloobs VALUES(?, ?, ?)", asList(expectBlobs[2])));

            helper.expectResultSetValues(conn.sendQuery("SELECT id, sha1(medium_one), sha1(long_one) FROM bloobs ORDER BY id"), expectHashes);
            helper.expectResultSetValues(conn.sendQuery("SELECT id, medium_one, long_one FROM bloobs ORDER BY id"), expectBlobs);

            helper.expectSuccess(conn.sendQuery("DROP TABLE bloobs"));

            helper.expectSuccess(conn.close());

            testFinished.complete(null);
        });
    }

    @Test
    public void testLargeBlobsWithPS() {
        TestHelper.runTest(60000, (conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS blobbers"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE blobbers(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "medium_one MEDIUMBLOB," +
                    "long_one LONGBLOB" +
                ")"
            ));

            byte[] mediumBytes = randomBytes(5115421);
            byte[] longBytes = randomBytes(34000000);

            byte[] mediumBytes2 = randomBytes(15321412);
            byte[] longBytes2 = randomBytes(19800099);

            Object[][] expectBlobs = {
                { 1, mediumBytes,  null       },
                { 2, null,         longBytes  },
                { 3, mediumBytes2, longBytes2 }
            };

            Object[][] expectHashes = {
                { 1, sha1(mediumBytes),  null             },
                { 2, null,               sha1(longBytes)  },
                { 3, sha1(mediumBytes2), sha1(longBytes2) }
            };

            helper.expectSuccess(conn.prepareStatement("INSERT INTO blobbers VALUES(?, ?, ?)"), ps -> {
                helper.expectSuccess(ps.execute(asList(expectBlobs[0])));
                helper.expectSuccess(ps.execute(asList(expectBlobs[1])));
                helper.expectSuccess(ps.execute(asList(expectBlobs[2])));

                helper.expectSuccess(conn.prepareStatement("SELECT id, sha1(medium_one), sha1(long_one) FROM blobbers ORDER BY id"), selectShaPs -> {
                    helper.expectResultSetValues(selectShaPs.execute(Collections.emptyList()), expectHashes);
                    helper.expectSuccess(selectShaPs.close());
                });

                helper.expectSuccess(conn.prepareStatement("SELECT id, medium_one, long_one FROM blobbers ORDER BY id"), selectPs -> {
                    helper.expectResultSetValues(selectPs.execute(Collections.emptyList()), expectBlobs);
                    helper.expectSuccess(selectPs.close());

                    helper.expectSuccess(conn.sendQuery("DROP TABLE blobbers"));
                    helper.expectSuccess(ps.close());

                    helper.expectSuccess(conn.close());

                    testFinished.complete(null);
                });
            });
        });
    }

    static byte[] randomBytes(int length) {
        Random rnd = new Random();
        byte[] result = new byte[length];
        rnd.nextBytes(result);
        return result;
    }

    static String sha1(byte[] bytes) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        messageDigest.update(bytes);

        return ByteBufUtils.toHexString(messageDigest.digest()).toLowerCase();
    }
}
