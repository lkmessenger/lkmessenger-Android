package org.linkmessenger.base.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.ViewConfiguration;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.linkmessenger.base.ui.components.URLSpanNoUnderline;
import org.linkmessenger.base.ui.components.URLSpanReplacement;
import org.linkmessenger.base.ui.components.browser.Browser;
import org.linkmessenger.base.ui.components.utils.LinkifyPort;
import org.thoughtcrime.securesms.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidUtilities {
    public static float density = 1;
    public static Point displaySize = new Point();
    public static float touchSlop;
    public static final RectF rectTmp = new RectF();
    public final static String TYPEFACE_ROBOTO_MEDIUM = "fonts/rmedium.ttf";
    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();
    public static Pattern BAD_CHARS_PATTERN = null;
    public static Pattern BAD_CHARS_MESSAGE_PATTERN = null;

    static {
        try {
            BAD_CHARS_PATTERN = Pattern.compile("[\u2500-\u25ff]");
            BAD_CHARS_MESSAGE_PATTERN = Pattern.compile("[\u2066-\u2067]+");
        }catch (Exception e){

        }
    }
    public static void checkDisplaySize(Context context, Configuration newConfiguration) {
        try{
            density = context.getResources().getDisplayMetrics().density;

            Configuration configuration = newConfiguration;
            if (configuration == null) {
                configuration = context.getResources().getConfiguration();
            }
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }
            ViewConfiguration vc = ViewConfiguration.get(context);
            touchSlop = vc.getScaledTouchSlop();
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage()!=null?e.getMessage():"checkDisplaySize error");
            density = 2.6f;
        }
    }
    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }
    public static int lerp(int a, int b, float f) {
        return (int) (a + f * (b - a));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static float lerp(float[] ab, float f) {
        return lerp(ab[0], ab[1], f);
    }

    public static void lerp(RectF a, RectF b, float f, RectF to) {
        if (to != null) {
            to.set(
                    AndroidUtilities.lerp(a.left, b.left, f),
                    AndroidUtilities.lerp(a.top, b.top, f),
                    AndroidUtilities.lerp(a.right, b.right, f),
                    AndroidUtilities.lerp(a.bottom, b.bottom, f)
            );
        }
    }

    public static void lerp(Rect a, Rect b, float f, Rect to) {
        if (to != null) {
            to.set(
                    AndroidUtilities.lerp(a.left, b.left, f),
                    AndroidUtilities.lerp(a.top, b.top, f),
                    AndroidUtilities.lerp(a.right, b.right, f),
                    AndroidUtilities.lerp(a.bottom, b.bottom, f)
            );
        }
    }
    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (ApplicationContext.applicationHandler == null) {
            return;
        }
        if (delay == 0) {
            ApplicationContext.applicationHandler.post(runnable);
        } else {
            ApplicationContext.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        if (ApplicationContext.applicationHandler == null) {
            return;
        }
        ApplicationContext.applicationHandler.removeCallbacks(runnable);
    }
    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t;
                    if (Build.VERSION.SDK_INT >= 26) {
                        Typeface.Builder builder = new Typeface.Builder(ApplicationContext.applicationContext.getAssets(), assetPath);
                        if (assetPath.contains("medium")) {
                            builder.setWeight(700);
                        }
                        if (assetPath.contains("italic")) {
                            builder.setItalic(true);
                        }
                        t = builder.build();
                    } else {
                        t = Typeface.createFromAsset(ApplicationContext.applicationContext.getAssets(), assetPath);
                    }
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
//                    if (BuildVars.LOGS_ENABLED) {
//                        FileLog.e("Could not get typeface '" + assetPath + "' because " + e.getMessage());
//                    }
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }
    private static boolean containsUnsupportedCharacters(String text) {
        if (text.contains("\u202C")) {
            return true;
        }
        if (text.contains("\u202D")) {
            return true;
        }
        if (text.contains("\u202E")) {
            return true;
        }
        try {
            if (BAD_CHARS_PATTERN.matcher(text).find()) {
                return true;
            }
        } catch (Throwable e) {
            return true;
        }
        return false;
    }
    public static String getSafeString(String str) {
        try {
            return BAD_CHARS_MESSAGE_PATTERN.matcher(str).replaceAll("\u200C");
        } catch (Throwable e) {
            return str;
        }
    }
    private static void gatherLinks(ArrayList<LinkSpec> links, Spannable s, Pattern pattern, String[] schemes, Linkify.MatchFilter matchFilter, boolean internalOnly) {
        if (TextUtils.indexOf(s, '─') >= 0) {
            s = new SpannableStringBuilder(s.toString().replace('─', ' '));
        }
        Matcher m = pattern.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();

            if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
                LinkSpec spec = new LinkSpec();

                String url = makeUrl(m.group(0), schemes, m);
                if (internalOnly && !Browser.isInternalUrl(url, true, null)) {
                    continue;
                }
                spec.url = url;
                spec.start = start;
                spec.end = end;

                links.add(spec);
            }
        }
    }

    public static boolean addLinks(Spannable text, int mask) {
        return addLinks(text, mask, false);
    }

    public static boolean addLinks(Spannable text, int mask, boolean internalOnly) {
        return addLinks(text, mask, internalOnly, true);
    }
    public static final Linkify.MatchFilter sUrlMatchFilter = (s, start, end) -> {
        if (start == 0) {
            return true;
        }
        if (s.charAt(start - 1) == '@') {
            return false;
        }
        return true;
    };

    public static boolean addLinks(Spannable text, int mask, boolean internalOnly, boolean removeOldReplacements) {
        if (text == null || containsUnsupportedCharacters(text.toString()) || mask == 0) {
            return false;
        }
        URLSpan[] old = text.getSpans(0, text.length(), URLSpan.class);
        for (int i = old.length - 1; i >= 0; i--) {
            URLSpan o = old[i];
            if (!(o instanceof URLSpanReplacement) || removeOldReplacements) {
                text.removeSpan(o);
            }
        }
        final ArrayList<LinkSpec> links = new ArrayList<>();
        if (!internalOnly && (mask & Linkify.PHONE_NUMBERS) != 0) {
            Linkify.addLinks(text, Linkify.PHONE_NUMBERS);
        }
        if ((mask & Linkify.WEB_URLS) != 0) {
            gatherLinks(links, text, LinkifyPort.WEB_URL, new String[]{"http://", "https://", "link://"}, sUrlMatchFilter, internalOnly);
        }
        pruneOverlaps(links);
        if (links.size() == 0) {
            return false;
        }
        for (int a = 0, N = links.size(); a < N; a++) {
            LinkSpec link = links.get(a);
            URLSpan[] oldSpans = text.getSpans(link.start, link.end, URLSpan.class);
            if (oldSpans != null && oldSpans.length > 0) {
                for (int b = 0; b < oldSpans.length; b++) {
                    URLSpan o = oldSpans[b];
                    text.removeSpan(o);

                    if (!(o instanceof URLSpanReplacement) || removeOldReplacements) {
                        text.removeSpan(o);
                    }
                }
            }
//            text.setSpan(new URLSpan(link.url), link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new URLSpanNoUnderline(link.url), link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return true;
    }
    private static void pruneOverlaps(ArrayList<LinkSpec> links) {
        Comparator<LinkSpec> c = (a, b) -> {
            if (a.start < b.start) {
                return -1;
            }
            if (a.start > b.start) {
                return 1;
            }
            if (a.end < b.end) {
                return 1;
            }
            if (a.end > b.end) {
                return -1;
            }
            return 0;
        };

        Collections.sort(links, c);

        int len = links.size();
        int i = 0;

        while (i < len - 1) {
            LinkSpec a = links.get(i);
            LinkSpec b = links.get(i + 1);
            int remove = -1;

            if ((a.start <= b.start) && (a.end > b.start)) {
                if (b.end <= a.end) {
                    remove = i + 1;
                } else if ((a.end - a.start) > (b.end - b.start)) {
                    remove = i + 1;
                } else if ((a.end - a.start) < (b.end - b.start)) {
                    remove = i;
                }
                if (remove != -1) {
                    links.remove(remove);
                    len--;
                    continue;
                }
            }
            i++;
        }
    }
    private static String makeUrl(String url, String[] prefixes, Matcher matcher) {
        boolean hasPrefix = false;
        for (int i = 0; i < prefixes.length; i++) {
            if (url.regionMatches(true, 0, prefixes[i], 0, prefixes[i].length())) {
                hasPrefix = true;
                if (!url.regionMatches(false, 0, prefixes[i], 0, prefixes[i].length())) {
                    url = prefixes[i] + url.substring(prefixes[i].length());
                }
                break;
            }
        }
        if (!hasPrefix && prefixes.length > 0) {
            url = prefixes[0] + url;
        }
        return url;
    }
    public static boolean shouldShowUrlInAlert(String url) {
        try {
            Uri uri = Uri.parse(url);
            url = uri.getHost();
            return checkHostForPunycode(url);
        } catch (Exception e) {
//            FileLog.e(e);
        }
        return false;
    }
    public static boolean checkHostForPunycode(String url) {
        if (url == null) {
            return false;
        }
        boolean hasLatin = false;
        boolean hasNonLatin = false;
        try {
            for (int a = 0, N = url.length(); a < N; a++) {
                char ch = url.charAt(a);
                if (ch == '.' || ch == '-' || ch == '/' || ch == '+' || ch >= '0' && ch <= '9') {
                    continue;
                }
                if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                    hasLatin = true;
                } else {
                    hasNonLatin = true;
                }
                if (hasLatin && hasNonLatin) {
                    break;
                }
            }
        } catch (Exception e) {
//            FileLog.e(e);
        }
        return hasLatin && hasNonLatin;
    }
    private static class LinkSpec {
        String url;
        int start;
        int end;
    }

}
