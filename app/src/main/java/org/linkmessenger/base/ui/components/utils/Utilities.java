package org.linkmessenger.base.ui.components.utils;

public class Utilities {
    public static Integer parseInt(CharSequence value) {
        if (value == null) {
            return 0;
        }
        int val = 0;
        try {
            int start = -1, end;
            for (end = 0; end < value.length(); ++end) {
                char character = value.charAt(end);
                boolean allowedChar = character == '-' || character >= '0' && character <= '9';
                if (allowedChar && start < 0) {
                    start = end;
                } else if (!allowedChar && start >= 0) {
                    end++;
                    break;
                }
            }
            if (start >= 0) {
                String str = value.subSequence(start, end).toString();
//                val = parseInt(str);
                val = Integer.parseInt(str);
            }
//            Matcher matcher = pattern.matcher(value);
//            if (matcher.find()) {
//                String num = matcher.group(0);
//                val = Integer.parseInt(num);
//            }
        } catch (Exception ignore) {}
        return val;
    }
}
