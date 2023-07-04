/*
 * This Java source file was generated by the Gradle 'init' task.
 */

package com.github.junit.params.parser.utilities;

import com.github.junit.params.parser.list.LinkedList;

final class JoinUtils {

    private JoinUtils() {}

    public static String join(LinkedList source) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < source.size(); ++i) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(source.get(i));
        }

        return result.toString();
    }

}
