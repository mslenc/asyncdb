package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class TextQueriesTest {
    @Test
    public void testSmallTexts() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS texties"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE texties(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "varchar_one VARCHAR(200)," +
                    "tiny_one TINYTEXT," +
                    "text_one TEXT," +
                    "medium_one MEDIUMTEXT," +
                    "long_one LONGTEXT," +
                    "char_one CHAR(99)" +
                ") CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
            ));

            String varChars = randomChars(166);
            String tinyChars = randomChars(177);
            String textChars = randomChars(5187);
            String mediumChars = randomChars(12110);
            String longChars = randomChars(155151);
            String chars = randomChars(55);

            String varChars2 = randomChars(146);
            String tinyChars2 = randomChars(157);
            String textChars2 = randomChars(5117);
            String mediumChars2 = randomChars(16666);
            String longChars2 = randomChars(298765);
            String chars2 = "This is extremely random, as well! Honest!";

            Object[][] expectChars = {
                    { 1, varChars,  null,       null,       null,         null,       null   },
                    { 2, null,      tinyChars,  null,       null,         null,       null   },
                    { 3, null,      null,       textChars,  null,         null,       null   },
                    { 4, null,      null,       null,       mediumChars,  null,       null   },
                    { 5, null,      null,       null,       null,         longChars,  null   },
                    { 6, null,      null,       null,       null,         null,       chars  },
                    { 7, varChars2, tinyChars2, textChars2, mediumChars2, longChars2, chars2 },
            };

            Object[][] expectHashes = {
                    { 1, sha1(varChars),  null,             null,             null,               null,             null         },
                    { 2, null,            sha1(tinyChars),  null,             null,               null,             null         },
                    { 3, null,            null,             sha1(textChars),  null,               null,             null         },
                    { 4, null,            null,             null,             sha1(mediumChars),  null,             null         },
                    { 5, null,            null,             null,             null,               sha1(longChars),  null         },
                    { 6, null,            null,             null,             null,               null,             sha1(chars)  },
                    { 7, sha1(varChars2), sha1(tinyChars2), sha1(textChars2), sha1(mediumChars2), sha1(longChars2), sha1(chars2) }
            };

            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[0])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[1])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[2])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[3])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[4])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[5])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO texties VALUES(?, ?, ?, ?, ?, ?, ?)", asList(expectChars[6])));

            helper.expectResultSetValues(conn.sendQuery("SELECT id, sha1(varchar_one), sha1(tiny_one), sha1(text_one), sha1(medium_one), sha1(long_one), sha1(char_one) FROM texties ORDER BY id"), expectHashes);
            helper.expectResultSetValues(conn.sendQuery("SELECT id, varchar_one, tiny_one, text_one, medium_one, long_one, char_one FROM texties ORDER BY id"), expectChars);

            helper.expectSuccess(conn.sendQuery("DROP TABLE texties"));

            helper.expectSuccess(conn.disconnect());

            testFinished.complete(null);
        });
    }

    @Test
    public void testSmallTextsWithPS() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS texties_ps"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE texties_ps(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "varchar_one VARCHAR(200)," +
                    "tiny_one TINYTEXT," +
                    "text_one TEXT," +
                    "medium_one MEDIUMTEXT," +
                    "long_one LONGTEXT," +
                    "char_one CHAR(99)" +
                ") CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
            ));

            String varChars = randomChars(156);
            String tinyChars = randomChars(200);
            String textChars = randomChars(5287);
            String mediumChars = randomChars(13110);
            String longChars = randomChars(145151);
            String chars = randomChars(65);

            String varChars2 = randomChars(126);
            String tinyChars2 = randomChars(217);
            String textChars2 = randomChars(6115);
            String mediumChars2 = randomChars(14444);
            String longChars2 = randomChars(300765);
            String chars2 = "This is not extremely random, however!";

            Object[][] expectChars = {
                    { 1, varChars,  null,       null,       null,         null,       null   },
                    { 2, null,      tinyChars,  null,       null,         null,       null   },
                    { 3, null,      null,       textChars,  null,         null,       null   },
                    { 4, null,      null,       null,       mediumChars,  null,       null   },
                    { 5, null,      null,       null,       null,         longChars,  null   },
                    { 6, null,      null,       null,       null,         null,       chars  },
                    { 7, varChars2, tinyChars2, textChars2, mediumChars2, longChars2, chars2 },
            };

            Object[][] expectHashes = {
                    { 1, sha1(varChars),  null,             null,             null,               null,             null         },
                    { 2, null,            sha1(tinyChars),  null,             null,               null,             null         },
                    { 3, null,            null,             sha1(textChars),  null,               null,             null         },
                    { 4, null,            null,             null,             sha1(mediumChars),  null,             null         },
                    { 5, null,            null,             null,             null,               sha1(longChars),  null         },
                    { 6, null,            null,             null,             null,               null,             sha1(chars)  },
                    { 7, sha1(varChars2), sha1(tinyChars2), sha1(textChars2), sha1(mediumChars2), sha1(longChars2), sha1(chars2) }
            };

            helper.expectSuccess(conn.prepareStatement("INSERT INTO texties_ps VALUES(?, ?, ?, ?, ?, ?, ?)"), ps -> {
                helper.expectSuccess(ps.execute(asList(expectChars[0])));
                helper.expectSuccess(ps.execute(asList(expectChars[1])));
                helper.expectSuccess(ps.execute(asList(expectChars[2])));
                helper.expectSuccess(ps.execute(asList(expectChars[3])));
                helper.expectSuccess(ps.execute(asList(expectChars[4])));
                helper.expectSuccess(ps.execute(asList(expectChars[5])));
                helper.expectSuccess(ps.execute(asList(expectChars[6])));
                helper.expectSuccess(ps.close(), ignored -> {

                    helper.expectResultSetValues(conn.sendQuery("SELECT id, sha1(varchar_one), sha1(tiny_one), sha1(text_one), sha1(medium_one), sha1(long_one), sha1(char_one) FROM texties_ps ORDER BY id"), expectHashes);

                    helper.expectSuccess(conn.prepareStatement("SELECT id, varchar_one, tiny_one, text_one, medium_one, long_one, char_one FROM texties_ps WHERE id=?"), readPs -> {

                        helper.expectResultSetValues(readPs.execute(singletonList(1)), new Object[][] { expectChars[0] });
                        helper.expectResultSetValues(readPs.execute(singletonList(2)), new Object[][] { expectChars[1] });
                        helper.expectResultSetValues(readPs.execute(singletonList(3)), new Object[][] { expectChars[2] });
                        helper.expectResultSetValues(readPs.execute(singletonList(4)), new Object[][] { expectChars[3] });
                        helper.expectResultSetValues(readPs.execute(singletonList(5)), new Object[][] { expectChars[4] });
                        helper.expectResultSetValues(readPs.execute(singletonList(6)), new Object[][] { expectChars[5] });
                        helper.expectResultSetValues(readPs.execute(singletonList(7)), new Object[][] { expectChars[6] });

                        helper.expectSuccess(readPs.close(), ignored2 -> {
                            helper.expectSuccess(conn.sendQuery("DROP TABLE texties_ps"));
                            helper.expectSuccess(conn.disconnect());
                            testFinished.complete(null);
                        });
                    });
                });
            });
        });
    }

    @Test
    public void testLargeTexts() {
        TestHelper.runTest(60000, (conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS textoobles"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE textoobles(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "medium_one MEDIUMTEXT," +
                    "long_one LONGTEXT" +
                ") CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
            ));

            String mediumChars = randomChars(15_777_412);
            String longChars = randomChars(77_654_321);

            String mediumChars2 = randomChars(14_131_211);
            String longChars2 = randomChars(39_210_101);

            Object[][] expectChars = {
                { 1, mediumChars,  null       },
                { 2, null,         longChars  },
                { 3, mediumChars2, longChars2 }
            };

            Object[][] expectHashes = {
                { 1, sha1(mediumChars),  null             },
                { 2, null,               sha1(longChars)  },
                { 3, sha1(mediumChars2), sha1(longChars2) }
            };

            helper.expectSuccess(conn.sendQuery("INSERT INTO textoobles VALUES(?, ?, ?)", asList(expectChars[0])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO textoobles VALUES(?, ?, ?)", asList(expectChars[1])));
            helper.expectSuccess(conn.sendQuery("INSERT INTO textoobles VALUES(?, ?, ?)", asList(expectChars[2])));

            helper.expectResultSetValues(conn.sendQuery("SELECT id, sha1(medium_one), sha1(long_one) FROM textoobles ORDER BY id"), expectHashes);
            helper.expectResultSetValues(conn.sendQuery("SELECT id, medium_one, long_one FROM textoobles ORDER BY id"), expectChars);

            helper.expectSuccess(conn.sendQuery("DROP TABLE textoobles"));

            helper.expectSuccess(conn.disconnect());

            testFinished.complete(null);
        });
    }

    @Test
    public void testLargeTextsWithPS() {
        TestHelper.runTest(60000, (conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS textoobles_ps"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE textoobles_ps(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "medium_one MEDIUMTEXT," +
                    "long_one LONGTEXT" +
                ") CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
            ));

            String mediumChars = randomChars(12_110_000);
            String longChars = randomChars(63_425_151);

            String mediumChars2 = randomChars(11_000_999);
            String longChars2 = randomChars(56_411_413);

            Object[][] expectChars = {
                { 1, mediumChars,  null       },
                { 2, null,         longChars  },
                { 3, mediumChars2, longChars2 }
            };

            // we'll also use hashes to verify the server agrees on the content (e.g. if we
            // accidentally XORed all the bytes in both directions with some value, it'd
            // look the same here, but not in the database)
            Object[][] expectHashes = {
                { 1, sha1(mediumChars),  null             },
                { 2, null,               sha1(longChars)  },
                { 3, sha1(mediumChars2), sha1(longChars2) }
            };

            helper.expectSuccess(conn.prepareStatement("INSERT INTO textoobles_ps VALUES(?, ?, ?)"), ps -> {
                helper.expectSuccess(ps.execute(asList(expectChars[0])));
                helper.expectSuccess(ps.execute(asList(expectChars[1])));
                helper.expectSuccess(ps.execute(asList(expectChars[2])));
                helper.expectSuccess(ps.close(), ignored -> {

                    helper.expectResultSetValues(conn.sendQuery("SELECT id, sha1(medium_one), sha1(long_one) FROM textoobles_ps ORDER BY id"), expectHashes);

                    helper.expectSuccess(conn.prepareStatement("SELECT id, medium_one, long_one FROM textoobles_ps WHERE id=?"), readPs -> {

                        helper.expectResultSetValues(readPs.execute(singletonList(1)), new Object[][] { expectChars[0] });
                        helper.expectResultSetValues(readPs.execute(singletonList(2)), new Object[][] { expectChars[1] });
                        helper.expectResultSetValues(readPs.execute(singletonList(3)), new Object[][] { expectChars[2] });

                        helper.expectSuccess(readPs.close(), ignored2 -> {
                            helper.expectSuccess(conn.sendQuery("DROP TABLE textoobles_ps"));
                            helper.expectSuccess(conn.disconnect());
                            testFinished.complete(null);
                        });
                    });
                });
            });
        });
    }

    static int utf8len(int codepoint) {
        if (codepoint <= 0x7f) return 1;
        if (codepoint <= 0x7ff) return 2;
        if (codepoint <= 0xffff) return 3;
        return 4;
    }

    private static final int[] validCodepoints;
    static {
        ArrayList<Integer> cps = new ArrayList<>();
        for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
            if (Character.isDefined(cp) && (cp > 0xFFFF || !Character.isSurrogate((char)cp))) {
                cps.add(cp);
            }
        }

        validCodepoints = new int[cps.size()];
        int i = 0;
        for (int cp : cps)
            validCodepoints[i++] = cp;
    }

    static String randomChars(int approxByteLength) {
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();

        int lengthRemain = approxByteLength;
        while (lengthRemain > 0) {
            int first = rnd.nextInt(validCodepoints.length);
            for (int a = 0; a < 8; a++) {
                int cp = validCodepoints[(first + a) % validCodepoints.length];
                sb.appendCodePoint(cp);
                lengthRemain -= utf8len(cp);
            }
        }

        return sb.toString();
    }

    static String sha1(String string) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        messageDigest.update(string.getBytes(StandardCharsets.UTF_8));

        return ByteBufUtils.toHexString(messageDigest.digest()).toLowerCase();
    }
}
