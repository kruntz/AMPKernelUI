package it.oltrenoi.ampkernelui.oled;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;

import java.io.IOException;

/**
 * Created by kruntz on 01/02/17.
 */

public class Ssd1306 {

    public static final short SSD1306_I2C_ADDRESS = 0x3C;
    public static final short SSD1306_SETCONTRAST = 0x81;
    public static final short SSD1306_DISPLAYALLON_RESUME = 0xA4;
    public static final short SSD1306_DISPLAYALLON = 0xA5;
    public static final short SSD1306_NORMALDISPLAY = 0xA6;
    public static final short SSD1306_INVERTDISPLAY = 0xA7;
    public static final short SSD1306_DISPLAYOFF = 0xAE;
    public static final short SSD1306_DISPLAYON = 0xAF;
    public static final short SSD1306_SETDISPLAYOFFSET = 0xD3;
    public static final short SSD1306_SETCOMPINS = 0xDA;
    public static final short SSD1306_SETVCOMDETECT = 0xDB;
    public static final short SSD1306_SETDISPLAYCLOCKDIV = 0xD5;
    public static final short SSD1306_SETPRECHARGE = 0xD9;
    public static final short SSD1306_SETMULTIPLEX = 0xA8;
    public static final short SSD1306_SETLOWCOLUMN = 0x00;
    public static final short SSD1306_SETHIGHCOLUMN = 0x10;
    public static final short SSD1306_SETSTARTLINE = 0x40;
    public static final short SSD1306_MEMORYMODE = 0x20;
    public static final short SSD1306_COLUMNADDR = 0x21;
    public static final short SSD1306_PAGEADDR = 0x22;
    public static final short SSD1306_COMSCANINC = 0xC0;
    public static final short SSD1306_COMSCANDEC = 0xC8;
    public static final short SSD1306_SEGREMAP = 0xA0;
    public static final short SSD1306_CHARGEPUMP = 0x8D;
    public static final short SSD1306_EXTERNALVCC = 0x1;
    public static final short SSD1306_SWITCHCAPVCC = 0x2;
    public static final short SSD1306_ACTIVATE_SCROLL = 0x2F;
    public static final short SSD1306_DEACTIVATE_SCROLL = 0x2E;
    public static final short SSD1306_SET_VERTICAL_SCROLL_AREA = 0xA3;
    public static final short SSD1306_RIGHT_HORIZONTAL_SCROLL = 0x26;
    public static final short SSD1306_LEFT_HORIZONTAL_SCROLL = 0x27;
    public static final short SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
    public static final short SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = 0x2A;
    private static final String TAG = Ssd1306.class.getSimpleName();
    private static final int DISPLAY_WIDTH = 128;
    private static final int DISPLAY_HEIGHT = 64;

    protected Bitmap bitmap;
    protected Canvas canvas;
    private int pages;
    private I2cDevice i2cDevice;
    private byte[] buffer;

    private Ssd1306(I2cDevice i2cDevice) {
        this.i2cDevice = i2cDevice;
        this.pages = (DISPLAY_HEIGHT / 8);
        this.buffer = new byte[DISPLAY_WIDTH * this.pages];

        this.bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.ALPHA_8);
        this.canvas = new Canvas(this.bitmap);
    }

    private void initDisplay() {
        if (DISPLAY_WIDTH == 128 && DISPLAY_HEIGHT == 64) {
            this.init(0x3F, 0x12, 0x80);
        } else if (DISPLAY_WIDTH == 128 && DISPLAY_HEIGHT == 32) {
            this.init(0x1F, 0x02, 0x80);
        } else if (DISPLAY_WIDTH == 96 && DISPLAY_HEIGHT == 16) {
            this.init(0x0F, 0x02, 0x60);
        }
    }

    private void init(int multiplex, int compins, int ratio) {
        this.command(SSD1306_DISPLAYOFF);
        this.command(SSD1306_SETDISPLAYCLOCKDIV);
        this.command((short) ratio);
        this.command(SSD1306_SETMULTIPLEX);
        this.command((short) multiplex);
        this.command(SSD1306_SETDISPLAYOFFSET);
        this.command((short) 0x0);
        this.command(SSD1306_SETSTARTLINE);
        this.command(SSD1306_CHARGEPUMP);
        this.command((short) 0x10);
        this.command(SSD1306_MEMORYMODE);
        this.command((short) 0x00);
        this.command((short) (SSD1306_SEGREMAP | 0x1));
        this.command(SSD1306_COMSCANDEC);
        this.command(SSD1306_SETCOMPINS);
        this.command((short) compins);
        this.command(SSD1306_SETCONTRAST);
        this.command((short) 0x9F);
        this.command(SSD1306_SETPRECHARGE);
        this.command((short) 0x22);
        this.command(SSD1306_SETVCOMDETECT);
        this.command((short) 0x40);
        this.command(SSD1306_DISPLAYALLON_RESUME);
        this.command(SSD1306_NORMALDISPLAY);
    }

    public void command(int command) {
        this.i2cWrite(0, command);
    }

    public void data(byte[] data) {
//        for (int i = 0; i < data.length; i += 16) {
//            this.i2cWrite(0x40, data[i]);
//        }
        try {
            this.i2cDevice.write(data, data.length);
        } catch (IOException e) {
            Log.w(TAG, "Unable write buffer to I2C device", e);
        }
    }

    public void begin() {
        this.initDisplay();
        this.command(SSD1306_DISPLAYON);
        this.clear();
        this.display();
    }

    /**
     * Sends the buffer to the display
     */
    public synchronized void display() {
        this.command(SSD1306_COLUMNADDR);
        this.command(0);
        this.command(DISPLAY_WIDTH - 1);
        this.command(SSD1306_PAGEADDR);
        this.command(0);
        this.command(this.pages - 1);

        this.data(this.buffer);
    }

    /**
     * Clears the buffer by creating a new byte array
     */
    public void clear() {
        this.buffer = new byte[DISPLAY_WIDTH * this.pages];
    }

    public void setContrast(byte contrast) {
        this.command(SSD1306_SETCONTRAST);
        this.command(contrast);
    }

    public void invertDisplay(boolean invert) {
        if (invert) {
            this.command(SSD1306_INVERTDISPLAY);
        } else {
            this.command(SSD1306_NORMALDISPLAY);
        }
    }

    public void scrollHorizontally(boolean left, int start, int end) {
        this.command(left ? SSD1306_LEFT_HORIZONTAL_SCROLL : SSD1306_RIGHT_HORIZONTAL_SCROLL);
        this.command(0);
        this.command(start);
        this.command(0);
        this.command(end);
        this.command(1);
        this.command(0xFF);
        this.command(SSD1306_ACTIVATE_SCROLL);
    }

    public void stopScroll() {
        this.command(SSD1306_DEACTIVATE_SCROLL);
    }

    public int getWidth() {
        return DISPLAY_WIDTH;
    }

    public int getHeight() {
        return DISPLAY_HEIGHT;
    }

    /**
     * Sets one pixel in the current buffer
     *
     * @param x     X position
     * @param y     Y position
     * @param white White or black pixel
     * @return True if the pixel was successfully set
     */
    public boolean setPixel(int x, int y, boolean white) {
        if (x < 0 || x > DISPLAY_WIDTH || y < 0 || y > DISPLAY_HEIGHT) {
            return false;
        }

        if (white) {
            this.buffer[x + (y / 8) * DISPLAY_WIDTH] |= (1 << (y & 7));
        } else {
            this.buffer[x + (y / 8) * DISPLAY_WIDTH] &= ~(1 << (y & 7));
        }

        return true;
    }

    public synchronized void displayImage() {
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                this.setPixel(x, y, Color.alpha(this.bitmap.getPixel(x, y)) > 0);
            }
        }

        this.display();
    }

    private void i2cWrite(int register, int value) {
        value &= 0xFF;
        byte _8bitVal = (byte) value;
        try {
            this.i2cDevice.writeRegByte(register, _8bitVal);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
        }
    }
}
