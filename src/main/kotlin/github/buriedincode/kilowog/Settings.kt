package github.buriedincode.kilowog

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.addResourceSource
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.Path
import java.nio.file.Paths

data class Comicvine(val apiKey: Secret?)
data class LeagueOfComicGeeks(val accessToken: Secret?, val clientId: String?, val clientSecret: Secret?)
data class Marvel(val publicKey: String?, val privateKey: Secret?)
data class Metron(val username: String?, val password: Secret?)
data class Settings(
    val collectionFolder: Path,
    val comicvine: Comicvine,
    val leagueOfComicGeeks: LeagueOfComicGeeks,
    val marvel: Marvel,
    val metron: Metron,
) {
    companion object : Logging {
        fun load(): Settings = ConfigLoaderBuilder.default()
            .addPathSource(
                Paths.get(System.getProperty("user.home"), ".config", "kilowog", "settings.yaml"),
                optional = true,
                allowEmpty = true,
            )
            .addPathSource(
                Paths.get(System.getProperty("user.home"), ".config", "kilowog", "settings.properties"),
                optional = true,
                allowEmpty = true,
            )
            .addResourceSource("/default.yaml")
            .build()
            .loadConfigOrThrow<Settings>()
    }
}
