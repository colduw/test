package lain.mods.cos.impl.update;

public record UpdateResult(VerStates verStates, String latestVersion, String recommendedVersion, String currentVersion) {
}
