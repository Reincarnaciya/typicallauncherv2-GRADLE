package space.typro.typicallauncher.models;

import lombok.Data;
import space.typro.typicallauncher.managers.DirManager;

import java.io.File;
import java.net.URL;

@Data
public class Server {
    String name;
    URL iconUrl;
    ServerVersion version;






    public enum ServerVersion {
        VERSION_1_7_10("assets1.7.10"), VERSION_1_12_2("assets1.12.2");

        public final String pathToAssets;

        ServerVersion(String assetName) {
            this.pathToAssets = DirManager.assetsDir.getDir().getAbsolutePath() + File.separator + assetName;
        }
    }

}
