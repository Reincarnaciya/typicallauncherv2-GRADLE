package space.typro.typicallauncher.managers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.controllers.scenes.subscenes.SettingsController;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class SettingsManager {

    public Map<SettingsController.GameSettings.SettingsEnum, String> detectChanges(SettingsController.GameSettings newSettings, SettingsUIComponents uiComponents) {
        Map<SettingsController.GameSettings.SettingsEnum, String> changes = new EnumMap<>(SettingsController.GameSettings.SettingsEnum.class);

        checkBoolean(changes, SettingsController.GameSettings.SettingsEnum.FULLSCREEN,
                newSettings::isFullscreen, uiComponents.fullscreenCheckBox::isSelected);
        checkBoolean(changes, SettingsController.GameSettings.SettingsEnum.HIDE_TO_TRAY,
                newSettings::isHideToTray, uiComponents.hideLauncherCheckBox::isSelected);
        checkFloat(changes, SettingsController.GameSettings.SettingsEnum.RAM,
                newSettings::getRam, () -> (float) uiComponents.ramSlider.getValue());
        checkInteger(changes, SettingsController.GameSettings.SettingsEnum.WIDTH,
                newSettings::getWidth, () -> parseTextField(uiComponents.widthTextField));
        checkInteger(changes, SettingsController.GameSettings.SettingsEnum.HEIGHT,
                newSettings::getHeight, () -> parseTextField(uiComponents.heightTextField));

        return changes;
    }

    private void checkBoolean(Map<SettingsController.GameSettings.SettingsEnum, String> changes,
                              SettingsController.GameSettings.SettingsEnum setting,
                              Supplier<Boolean> currentValue,
                              Supplier<Boolean> newValue) {
        if (!currentValue.get().equals(newValue.get())) {
            changes.put(setting, String.valueOf(newValue.get()));
        }
    }

    private void checkInteger(Map<SettingsController.GameSettings.SettingsEnum, String> changes,
                              SettingsController.GameSettings.SettingsEnum setting,
                              Supplier<Integer> currentValue,
                              Supplier<Integer> newValue) {
        try {
            int newVal = newValue.get();
            if (currentValue.get() != newVal) {
                changes.put(setting, String.valueOf(newVal));
            }
        } catch (NumberFormatException e) {
            log.error(String.format("Invalid integer value for {%s}", setting), e);
        }
    }

    private void checkFloat(Map<SettingsController.GameSettings.SettingsEnum, String> changes,
                            SettingsController.GameSettings.SettingsEnum setting,
                            Supplier<Float> currentValue,
                            Supplier<Float> newValue) {
        float newVal = newValue.get();
        if (currentValue.get() != newVal) {
            changes.put(setting, String.valueOf(newVal));
        }
    }

    private int parseTextField(TextField field) throws NumberFormatException {
        return Integer.parseInt(field.getText());
    }

    public static class SettingsUIComponents {
        public final CheckBox fullscreenCheckBox;
        public final CheckBox hideLauncherCheckBox;
        public final Slider ramSlider;
        public final TextField widthTextField;
        public final TextField heightTextField;

        public SettingsUIComponents(CheckBox fullscreen, CheckBox hideLauncher,
                                    Slider ram, TextField width, TextField height) {
            this.fullscreenCheckBox = fullscreen;
            this.hideLauncherCheckBox = hideLauncher;
            this.ramSlider = ram;
            this.widthTextField = width;
            this.heightTextField = height;
        }
    }
}
