package com.dcnh35.gradle
class Utils {

    public static boolean is140orAbove() {
        return Utils.versionCompare(com.android.builder.Version.ANDROID_GRADLE_PLUGIN_VERSION, "1.4.0") >= 0;
    }

    private static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("-")[0].split("\\.");
        String[] vals2 = str2.split("-")[0].split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }

        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }
}