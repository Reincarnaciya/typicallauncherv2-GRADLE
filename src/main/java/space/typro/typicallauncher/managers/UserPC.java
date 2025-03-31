package space.typro.typicallauncher.managers;


import com.sun.management.OperatingSystemMXBean;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.lang.management.ManagementFactory;

@Slf4j
@ToString
public class UserPC {
    /**
     * Физическая память ПК
     */
    private static final int RAM = (int) ((((OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean())
            .getTotalMemorySize()) / 1048576);
    public static final OS USER_OS = getPlatform();
    public static final String JAVA_BIT = System.getProperty("sun.arch.data.model");
    public static final int MONITOR_WIDTH = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getWidth();
    public static final int MONITOR_HEIGHT = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getHeight();



    private static OS getPlatform(){
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OS.WINDOWS;
        else if (os.contains("linux") || os.contains("unix")) return OS.LINUX;
        else if (os.contains("macos")) return OS.MACOS;
        else return OS.UNKNOWN;
    }

    /**
     * Возвращает доступное для java кол-во ОЗУ в мегабайтах
     * @return Доступная для Java ОЗУ в мегабайтах
     */
    public static int getAvailableRam(){
        if (JAVA_BIT.equalsIgnoreCase("32")) return 1536;
        return RAM/2;
    }

    public enum OS{
        WINDOWS, LINUX, MACOS, UNKNOWN
    }
}
