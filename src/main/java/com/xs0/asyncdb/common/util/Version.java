package com.xs0.asyncdb.common.util;

public class Version implements Comparable<Version> {
    public final int major;
    public final int minor;
    public final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public int compareTo(Version other) {
        if (major != other.major)
            return Integer.compare(major, other.major);

        if (minor != other.minor)
            return Integer.compare(minor, other.minor);

        return Integer.compare(patch, other.patch);
    }

    public static Version parse(String version) {
        String[] parts = version.split("[.]");
        return new Version(
            safeParse(parts, 0),
            safeParse(parts, 1),
            safeParse(parts, 2)
        );
    }

    private static int safeParse(String[] pieces, int index) {
        if (index >= pieces.length)
            return 0;

        try {
            return Integer.parseInt(pieces[index]);
        } catch (Exception e) {
            return 0;
        }
    }
}
