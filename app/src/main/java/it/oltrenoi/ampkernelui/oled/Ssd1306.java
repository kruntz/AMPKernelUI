package it.oltrenoi.ampkernelui.oled;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;

public class Ssd1306 implements Closeable {

    private static final String TAG = Ssd1306.class.getSimpleName();

    // I2C Slave Address
    private static final int I2C_ADDRESS = 0x78;

    // Display size
    private static final int DISPLAY_WIDTH = 128;
    private static final int DISPLAY_HEIGHT = 64;

    private static final int COMMAND_DISPLAY_ON = 0xAF;
    private static final int COMMAND_DISPLAY_OFF = 0xAE;

    private static final int SET_STARTLINE = 0x40;

    private static final int INIT_SEGREMAP = 0xA0;

    private static final int INIT_COMSCANDEC = 0xC8;
    private static final int INIT_DUTY_CYCLE_1_64 = 0x3F;

    private static final int INIT_CLK_DIV = 0xD5;
    private static final int INIT_RESISTER_RATIO = 0x80;

    private static final int INIT_MEMORY_MODE = 0x20;
    private static final int INIT_MEMORY_HORIZ = 0x00;

    private static final int INIT_DISPLAY_OFFSET = 0xD3;
    private static final int INIT_DISPLAY_NO_OFFSET = 0x00;

    private static final int SSD1306_SETCONTRAST = 0x81;

    private static final int SSD1306_DISPLAYALLON_RESUME = 0xA4;
    private static final int SSD1306_DISPLAYALLON = 0xA5;

    private static final int SSD1306_NORMALDISPLAY = 0xA6;
    private static final int SSD1306_INVERTDISPLAY = 0xA7;

    private static final int SSD1306_SETCOMPINS = 0xDA;
    private static final int SSD1306_SETVCOMDETECT = 0xDB;
    private static final int SSD1306_SETPRECHARGE = 0xD9;
    private static final int COMMAND_SETMULTIPLEX = 0xA8;
    private static final int SSD1306_SETLOWCOLUMN = 0x00;
    private static final int SSD1306_SETHIGHCOLUMN = 0x10;

    private static final int SSD1306_COLUMNADDR = 0x21;
    private static final int SSD1306_PAGEADDR = 0x22;

    private static final int SSD1306_COMSCANINC = 0xC0;
    private static final int SSD1306_CHARGEPUMP = 0x8D;
    private static final int SSD1306_EXTERNALVCC = 0x1;
    private static final int SSD1306_SWITCHCAPVCC = 0x2;

    // Scroll
    private static final short COMMAND_ACTIVATE_SCROLL = 0x2F;
    private static final short COMMAND_DEACTIVATE_SCROLL = 0x2E;
    private static final short COMMAND_RIGHT_HORIZONTAL_SCROLL = 0x26;
    private static final short COMMAND_LEFT_HORIZONTAL_SCROLL = 0x27;
    private static final short COMMAND_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
    private static final short COMMAND_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = 0x2A;
    private static final short SET_VERTICAL_SCROLL_AREA = 0xA3;

    private Bitmap bitmap;
    private Canvas canvas;
    private int pages;
    private I2cDevice i2cDevice;
    private byte[] buffer;

    public Ssd1306(String i2DeviceName) throws IOException {
        this.i2cDevice = new PeripheralManagerService().openI2cDevice(i2DeviceName, I2C_ADDRESS);
        init();
        this.pages = (DISPLAY_HEIGHT / 8);
        this.buffer = new byte[DISPLAY_WIDTH * this.pages];

        this.bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.ALPHA_8);
        this.canvas = new Canvas(this.bitmap);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void close() throws IOException {
        Log.d(TAG, "close");
        if (this.i2cDevice != null) {
            try {
                this.i2cDevice.close();
            } finally {
                this.i2cDevice = null;
            }
        }
    }

    private void init() {
        Log.d(TAG, "init");

        // Recommended initialization sequence based on http://goo.gl/VSu0C8
        // Step 1: Start with the display off
        this.command(COMMAND_DISPLAY_OFF);

        // Step 2: Set up the required communication / power settings
        this.command(INIT_SEGREMAP | 0x1);

        this.command(INIT_COMSCANDEC);
        this.command(INIT_DUTY_CYCLE_1_64);

        this.command(INIT_CLK_DIV);
        this.command(INIT_RESISTER_RATIO);

        // Step 3: Set display input configuration and start.
        // This will start the display all on
        // START_LINE present the memory-based mBuffer to the screen
        this.command(INIT_MEMORY_MODE);
        this.command(INIT_MEMORY_HORIZ);

        this.command(INIT_DISPLAY_OFFSET);
        this.command(INIT_DISPLAY_NO_OFFSET);

        this.command(COMMAND_DISPLAY_ON);

        this.command(SSD1306_CHARGEPUMP);

        // OPTIONALS
        //        this.command(COMMAND_SETMULTIPLEX);
        //        this.command(SSD1306_CHARGEPUMP);
        //        this.command(0x10);
        //        this.command(SSD1306_SETCOMPINS);
        //        this.command(0x12);
        //        this.command(SSD1306_SETCONTRAST);
        //        this.command(0x9F);
        //        this.command(SSD1306_SETPRECHARGE);
        //        this.command(0x22);
        //        this.command(SSD1306_SETVCOMDETECT);
        //        this.command(0x40);
        //        this.command(SSD1306_DISPLAYALLON_RESUME);
        //        this.command(SSD1306_NORMALDISPLAY);
        // OPTIONALS

        this.command(SET_STARTLINE);
    }

    public void begin() {
        this.init();
        this.command(COMMAND_DISPLAY_ON);
        this.clear();
        this.display();
    }

    /**
     * Sends the buffer to the display
     */
    private synchronized void display() {
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
    private void clear() {
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
        this.command(left ? COMMAND_LEFT_HORIZONTAL_SCROLL : COMMAND_RIGHT_HORIZONTAL_SCROLL);
        this.command(0);
        this.command(start);
        this.command(0);
        this.command(end);
        this.command(1);
        this.command(0xFF);
        this.command(COMMAND_ACTIVATE_SCROLL);
    }

    public void stopScroll() {
        this.command(COMMAND_DEACTIVATE_SCROLL);
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
    private boolean setPixel(int x, int y, boolean white) {
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

    private void command(int command) {
        this.i2cWrite(0, command);
    }

    private void data(byte[] data) {
        try {
            this.i2cDevice.write(data, data.length);
        } catch (IOException e) {
            Log.w(TAG, "Unable write buffer to I2C device", e);
        }
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
