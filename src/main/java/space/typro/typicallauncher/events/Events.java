package space.typro.typicallauncher.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import space.typro.typicallauncher.controllers.scenes.subscenes.SettingsController;

import java.util.HashMap;

public class Events {

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class InternetEventData extends EventData {
        InternetEventType type;

        public enum InternetEventType {
            LOST, RESTORED
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class UserEventData extends EventData {

        public enum UserEventType{
            LOGIN, REGISTER, LOGOUT
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class SettingsEventData extends EventData {
        SettingsEventType SettingsEventType;
        HashMap<String, String> changedSettings;
        SettingsController.GameSettings beforeChange;
        SettingsController.GameSettings newSettings;

        public enum SettingsEventType {
            CHANGE, SAVE, RESTORE, CANCEL_EDIT
        }
    }
    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class DownloadEvent extends EventData {
        DownloadEventType DownloadEventType;
        String localFilePath;
        int progressPercent;
        long totalFileSize;
        long downloaded;

        public enum DownloadEventType {
            START, FINISH, PROGRESS_UPDATE
        }
    }
}
