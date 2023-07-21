package org.linkmessenger.base.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextPaint;
import android.util.StateSet;

import androidx.core.content.ContextCompat;

import org.linkmessenger.base.ui.components.LinkPath;
import org.thoughtcrime.securesms.R;

import java.lang.reflect.Method;

public class Theme {
    private static Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static TextPaint profile_aboutTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    public static Paint chat_urlPaint;
    static {
        chat_urlPaint = new Paint();
        chat_urlPaint.setPathEffect(LinkPath.getRoundedEffect());
        profile_aboutTextPaint.setTextSize(AndroidUtilities.dp(16));
    }

    public static void setDrawableColor(Drawable drawable, int color) {
        if (drawable == null) {
            return;
        }
        if (drawable instanceof ShapeDrawable) {
            ((ShapeDrawable) drawable).getPaint().setColor(color);
        } else {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        }
    }
    @TargetApi(21)
    @SuppressLint("DiscouragedPrivateApi")
    public static void setRippleDrawableForceSoftware(RippleDrawable drawable) {
        if (drawable == null) {
            return;
        }
        try {
            Method method = RippleDrawable.class.getDeclaredMethod("setForceSoftware", boolean.class);
            method.invoke(drawable, true);
        } catch (Throwable ignore) {

        }
    }
    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor, int maskColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        pressedDrawable.getPaint().setColor(maskColor);
        if (Build.VERSION.SDK_INT >= 21) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{pressedColor}
            );
            return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
            stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
            return stateListDrawable;
        }
    }

    public static Drawable getSelectorDrawable(Context context, int color, boolean whiteBackground) {
        if (whiteBackground) {
            return getSelectorDrawable(color, ContextCompat.getColor(context, R.color.signal_colorBackground));
        } else {
            return createSelectorDrawable(color, 2);
        }
    }
    public static Drawable createRadSelectorDrawable(int color, int topRad, int bottomRad) {
        if (Build.VERSION.SDK_INT >= 21) {
            maskPaint.setColor(0xffffffff);
            Drawable maskDrawable = new RippleRadMaskDrawable(topRad, bottomRad);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            return new RippleDrawable(colorStateList, null, maskDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }
    public static Drawable getSelectorDrawable(int color, int backgroundColor) {
        if (backgroundColor != 0) {
            if (Build.VERSION.SDK_INT >= 21) {
                Drawable maskDrawable = new ColorDrawable(0xffffffff);
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{color}
                );
                return new RippleDrawable(colorStateList, new ColorDrawable(backgroundColor), maskDrawable);
            } else {
                StateListDrawable stateListDrawable = new StateListDrawable();
                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
                stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
                stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(backgroundColor));
                return stateListDrawable;
            }
        } else {
            return createSelectorDrawable(color, 2);
        }
    }
    public static Drawable createSelectorDrawable(int color) {
        return createSelectorDrawable(color, RIPPLE_MASK_CIRCLE_20DP, -1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType) {
        return createSelectorDrawable(color, maskType, -1);
    }
    public static final int RIPPLE_MASK_CIRCLE_20DP = 1;
    public static final int RIPPLE_MASK_ALL = 2;
    public static final int RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE = 3;
    public static final int RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4;
    public static final int RIPPLE_MASK_CIRCLE_AUTO = 5;
    public static final int RIPPLE_MASK_ROUNDRECT_6DP = 7;

    public static Drawable createSelectorDrawable(int color, int maskType, int radius) {
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = null;
            if ((maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 5) && Build.VERSION.SDK_INT >= 23) {
                maskDrawable = null;
            } else if (
                    maskType == RIPPLE_MASK_CIRCLE_20DP ||
                            maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE ||
                            maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER ||
                            maskType == RIPPLE_MASK_CIRCLE_AUTO ||
                            maskType == 6 ||
                            maskType == RIPPLE_MASK_ROUNDRECT_6DP
            ) {
                maskPaint.setColor(0xffffffff);
                maskDrawable = new Drawable() {

                    RectF rect;

                    @Override
                    public void draw(Canvas canvas) {
                        android.graphics.Rect bounds = getBounds();
                        if (maskType == RIPPLE_MASK_ROUNDRECT_6DP) {
                            if (rect == null) {
                                rect = new RectF();
                            }
                            rect.set(bounds);
                            float rad = radius <= 0 ? AndroidUtilities.dp(6) : radius;
                            canvas.drawRoundRect(rect, rad, rad, maskPaint);
                        } else {
                            int rad;
                            if (maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 6) {
                                rad = radius <= 0 ? AndroidUtilities.dp(20) : radius;
                            } else if (maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE) {
                                rad = (Math.max(bounds.width(), bounds.height()) / 2);
                            } else {
                                // RIPPLE_MASK_CIRCLE_AUTO = 5
                                // RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4
                                rad = (int) Math.ceil(Math.sqrt((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX()) + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())));
                            }
                            canvas.drawCircle(bounds.centerX(), bounds.centerY(), rad, maskPaint);
                        }
                    }

                    @Override
                    public void setAlpha(int alpha) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return PixelFormat.UNKNOWN;
                    }
                };
            } else if (maskType == RIPPLE_MASK_ALL) {
                maskDrawable = new ColorDrawable(0xffffffff);
            }
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{ StateSet.WILD_CARD },
                    new int[]{ color }
            );
            RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, null, maskDrawable);
            if (Build.VERSION.SDK_INT >= 23) {
                if (maskType == RIPPLE_MASK_CIRCLE_20DP) {
                    rippleDrawable.setRadius(radius <= 0 ? AndroidUtilities.dp(20) : radius);
                } else if (maskType == RIPPLE_MASK_CIRCLE_AUTO) {
                    rippleDrawable.setRadius(RippleDrawable.RADIUS_AUTO);
                }
            }
            return rippleDrawable;
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }
    public static void setSelectorDrawableColor(Drawable drawable, int color, boolean selected) {
        if (drawable instanceof StateListDrawable) {
            try {
                Drawable state;
                if (selected) {
                    state = getStateDrawable(drawable, 0);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                    state = getStateDrawable(drawable, 1);
                } else {
                    state = getStateDrawable(drawable, 2);
                }
                if (state instanceof ShapeDrawable) {
                    ((ShapeDrawable) state).getPaint().setColor(color);
                } else {
                    state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
            } catch (Throwable ignore) {

            }
        } else if (Build.VERSION.SDK_INT >= 21 && drawable instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) drawable;
            if (selected) {
                rippleDrawable.setColor(new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{color}
                ));
            } else {
                if (rippleDrawable.getNumberOfLayers() > 0) {
                    Drawable drawable1 = rippleDrawable.getDrawable(0);
                    if (drawable1 instanceof ShapeDrawable) {
                        ((ShapeDrawable) drawable1).getPaint().setColor(color);
                    } else {
                        drawable1.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            }
        }
    }
    private static Method StateListDrawable_getStateDrawableMethod;
    @SuppressLint("PrivateApi")
    private static Drawable getStateDrawable(Drawable drawable, int index) {
        if (Build.VERSION.SDK_INT >= 29 && drawable instanceof StateListDrawable) {
            return ((StateListDrawable) drawable).getStateDrawable(index);
        } else {
            if (StateListDrawable_getStateDrawableMethod == null) {
                try {
                    StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
                } catch (Throwable ignore) {

                }
            }
            if (StateListDrawable_getStateDrawableMethod == null) {
                return null;
            }
            try {
                return (Drawable) StateListDrawable_getStateDrawableMethod.invoke(drawable, index);
            } catch (Exception ignore) {

            }
            return null;
        }
    }
    public static void setMaskDrawableRad(Drawable rippleDrawable, int top, int bottom) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (rippleDrawable instanceof RippleDrawable) {
            RippleDrawable drawable = (RippleDrawable) rippleDrawable;
            int count = drawable.getNumberOfLayers();
            for (int a = 0; a < count; a++) {
                Drawable layer = drawable.getDrawable(a);
                if (layer instanceof RippleRadMaskDrawable) {
                    drawable.setDrawableByLayerId(android.R.id.mask, new RippleRadMaskDrawable(top, bottom));
                    break;
                }
            }
        }
    }
    public static class RippleRadMaskDrawable extends Drawable {
        private Path path = new Path();
        private float[] radii = new float[8];
        boolean invalidatePath = true;

        public RippleRadMaskDrawable(float top, float bottom) {
            radii[0] = radii[1] = radii[2] = radii[3] = AndroidUtilities.dp(top);
            radii[4] = radii[5] = radii[6] = radii[7] = AndroidUtilities.dp(bottom);
        }
        public RippleRadMaskDrawable(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            radii[0] = radii[1] = AndroidUtilities.dp(topLeft);
            radii[2] = radii[3] = AndroidUtilities.dp(topRight);
            radii[4] = radii[5] = AndroidUtilities.dp(bottomRight);
            radii[6] = radii[7] = AndroidUtilities.dp(bottomLeft);
        }

        public void setRadius(float top, float bottom) {
            radii[0] = radii[1] = radii[2] = radii[3] = AndroidUtilities.dp(top);
            radii[4] = radii[5] = radii[6] = radii[7] = AndroidUtilities.dp(bottom);
            invalidatePath = true;
            invalidateSelf();
        }
        public void setRadius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            radii[0] = radii[1] = AndroidUtilities.dp(topLeft);
            radii[2] = radii[3] = AndroidUtilities.dp(topRight);
            radii[4] = radii[5] = AndroidUtilities.dp(bottomRight);
            radii[6] = radii[7] = AndroidUtilities.dp(bottomLeft);
            invalidatePath = true;
            invalidateSelf();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            invalidatePath = true;
        }

        @Override
        public void draw(Canvas canvas) {
            if (invalidatePath) {
                invalidatePath = false;
                path.reset();
                AndroidUtilities.rectTmp.set(getBounds());
                path.addRoundRect(AndroidUtilities.rectTmp, radii, Path.Direction.CW);
            }
            canvas.drawPath(path, maskPaint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }
}
