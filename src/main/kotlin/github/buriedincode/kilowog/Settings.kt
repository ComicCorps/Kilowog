package github.buriedincode.kilowog

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.addResourceSource
import java.nio.file.Path
import kotlin.io.path.div

data class Settings(
    val collectionFolder: Path,
    val comicvine: Comicvine = Comicvine(),
    val leagueOfComicGeeks: LeagueOfComicGeeks = LeagueOfComicGeeks(),
    val marvel: Marvel = Marvel(),
    val metron: Metron = Metron(),
) {
    data class Comicvine(val apiKey: Secret? = null)

    data class LeagueOfComicGeeks(val accessToken: Secret? = null, val clientId: String? = null, val clientSecret: Secret? = null)

    data class Marvel(val publicKey: String? = null, val privateKey: Secret? = null)

    data class Metron(val username: String? = null, val password: Secret? = null)

    companion object {
        fun load(): Settings =
            ConfigLoaderBuilder.default()
                .addPathSource(Utils.CONFIG_ROOT / "settings.properties", optional = true, allowEmpty = true)
                .addResourceSource("/default.properties")
                .build()
                .loadConfigOrThrow<Settings>()
    }
}
