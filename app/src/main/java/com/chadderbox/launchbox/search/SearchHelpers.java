package com.chadderbox.launchbox.search;

public final class SearchHelpers {

    private SearchHelpers() { }

    // Very basic implementation
    // https://www.baeldung.com/java-levenshtein-distance
    // https://stackoverflow.com/q/36472793/25331601
    // https://stackoverflow.com/questions/79254268/levenshtein-distance-algorithm-without-delete-operation
    public static int calculateLevenshteinDistance(CharSequence a, CharSequence b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Inputs must not be null");
        }

        var lenA = a.length();
        var lenB = b.length();

        if (lenA > lenB) {
            var temp = a;
            a = b;
            b = temp;
            var tempLen = lenA;
            lenA = lenB;
            lenB = tempLen;
        }

        if (lenA == 0) {
            return lenB;
        }

        var prev = new int[lenA + 1];
        var curr = new int[lenA + 1];
        for (var i = 0; i <= lenA; i++) {
            prev[i] = i;
        }

        for (var j = 1; j <= lenB; j++) {
            char bj = b.charAt(j - 1);
            curr[0] = j;

            for (var i = 1; i <= lenA; i++) {
                var cost = a.charAt(i - 1) == bj ? 0 : 1;
                curr[i] = Math.min(Math.min(curr[i - 1] + 1, prev[i] + 1), prev[i - 1] + cost);
            }

            var temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[lenA];
    }

}
