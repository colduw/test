package lain.mods.cos.impl.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UpdateCheck {
    protected UpdateResult result;

    private UpdateResult updateResp(String modid) throws IOException, VersionParsingException {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modid);
        if (modContainer.isEmpty()) {
            throw new IllegalStateException("Could not find ModContainer for mod " + modid);
        }

        CustomValue updateJsonUrlValue = modContainer.get().getMetadata().getCustomValue("updateJSONURL");
        if (updateJsonUrlValue == null || !updateJsonUrlValue.getType().equals(CustomValue.CvType.STRING)) {
            throw new IllegalStateException("Could not find updateJSONURL for mod " + modid);
        }

        Optional<ModContainer> optionalMinecraft = FabricLoader.getInstance().getModContainer("minecraft");
        if (optionalMinecraft.isEmpty()) {
            throw new IllegalStateException("Could not find Minecraft mod container");
        }

        // mod version
        String modVersion = modContainer.get().getMetadata().getVersion().getFriendlyString();

        // mc version
        String mcVersion = optionalMinecraft.get().getMetadata().getVersion().getFriendlyString();

        // actually reading the thing
        String respStr = httpReq(updateJsonUrlValue.getAsString(), modid, modVersion, mcVersion);
        JsonObject resp = JsonParser.parseString(respStr).getAsJsonObject();
        JsonObject promos = resp.getAsJsonObject("promos");
        if (promos.isEmpty()) {
            throw new IllegalStateException("Could not find version promos for mod " + modid);
        }

        String latestVersion = SemanticVersion.parse(promos.get(mcVersion+"-latest").getAsString()).getFriendlyString();
        String recommendedVersion = SemanticVersion.parse(promos.get(mcVersion+"-recommended").getAsString()).getFriendlyString();

        // check if latest
        if (modVersion.equals(latestVersion)) {
            return new UpdateResult(VerStates.LATEST, latestVersion, recommendedVersion, modVersion);
        } else if (modVersion.compareTo(latestVersion) > 0) {
            return new UpdateResult(VerStates.AHEAD, latestVersion, recommendedVersion, modVersion);
        }

        // not latest nor ahead
        if (modVersion.equals(recommendedVersion)) {
            return new UpdateResult(VerStates.RECOMMENDED, latestVersion, recommendedVersion, modVersion);
        } else if (modVersion.compareTo(recommendedVersion) > 0) {
            return new UpdateResult(VerStates.BEHIND_LATEST_AHEAD_RECOMMENDED, latestVersion, recommendedVersion, modVersion);
        } else {
            return new UpdateResult(VerStates.BEHIND, latestVersion, recommendedVersion, modVersion);
        }
    }

    private String httpReq(String reqURL, String modid, String modver, String mcver) throws IOException {
        URL url = new URL(reqURL);
        HttpURLConnection req = (HttpURLConnection) url.openConnection();
        req.setInstanceFollowRedirects(true);
        req.setRequestMethod("GET");
        req.setConnectTimeout(5000);
        req.setReadTimeout(5000);
        req.setRequestProperty("User-Agent", String.format("%s version %s for Minecraft %s", modid, modver, mcver));

        int responseCode = req.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Unexpected error code " + responseCode);
        }

        String resp;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()))) {
            resp = in.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        req.disconnect();

        return resp;
    }

    public void asyncUpdate(String modid, Consumer<UpdateResult> callback)  {
        synchronized (this) {
            if (result != null) {
                callback.accept(result);
                return;
            }
        }

        Thread thread = new Thread(() -> {
            UpdateResult tryResult = tryCheck(modid);
            synchronized (this) {
                result = tryResult;
                callback.accept(result);
            }
        });

        thread.setName(String.format("%s_update_checker", modid));
        thread.setDaemon(true);
        thread.start();
    }

    private UpdateResult tryCheck(String modid) {
        try {
            return updateResp(modid);
        } catch (Exception ex) {
            return new UpdateResult(VerStates.ERROR, "-", "-", ex.getMessage());
        }
    }

    protected static UpdateCheck updateMan;

    public static UpdateCheck updateManager() {
        if (updateMan == null) {
            updateMan = new UpdateCheck();
        }

        return updateMan;
    }
}