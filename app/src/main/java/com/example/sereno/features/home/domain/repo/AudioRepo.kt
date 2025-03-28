package com.example.sereno.features.home.domain.repo

import com.example.sereno.features.home.data.AudioSettingsManager
import com.example.sereno.features.home.domain.model.CustomAudio
import com.example.sereno.features.home.domain.model.NormalAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRepo {
    suspend fun getNormalAudio(): List<NormalAudio> {
        val normalAudios = withContext(Dispatchers.IO) {
            listOf(
                NormalAudio(
                    id = 1,
                    title = "Beach",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://static.vecteezy.com/system/resources/thumbnails/012/400/885/small_2x/tropical-sunset-beach-and-sky-background-as-exotic-summer-landscape-with-beach-swing-or-hammock-and-white-sand-and-calm-sea-beach-banner-paradise-island-beach-vacation-or-summer-holiday-destination-photo.jpg",
                    subtitle = "Music with soft and binaural pads",
                    isPlaying = false
                ),
                NormalAudio(
                    id = 2,
                    title = "Fire",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://thewildlife.blog/wp-content/uploads/2023/06/daf20fe7-7922-4444-9772-8e6f79174221.jpg",
                    subtitle = "Gentle bells and flute",
                    isPlaying = false
                ),
                NormalAudio(
                    id = 3,
                    title = "Thunderstorm",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://www.usatoday.com/gcdn/presto/2023/06/23/USAT/4448a61c-3fad-466c-bb56-f704f34c7fd5-felix-mittermeier-Zkx_DgMQink-unsplash.jpg?crop=1436,1077,x0,y0",
                    subtitle = "Anxiety-free sound effect",
                    isPlaying = false
                ),
                NormalAudio(
                    id = 4,
                    title = "Big city",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://images.unsplash.com/photo-1515963665762-77ef90e624fa?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8YmlnJTIwY2l0eXxlbnwwfHwwfHx8MA%3D%3D",
                    subtitle = "Fall asleep fast",
                    isPlaying = false
                ),
                NormalAudio(
                    id = 5,
                    title = "Bird chirping",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://gl.audubon.org/sites/default/files/styles/article_hero_inline/public/aud_apa-2020_barn-swallow_a1-11258-1_nape_photo-xianwei-zeng.jpg?itok=hEJFNc9a",
                    subtitle = "Soft and light track",
                    isPlaying = false
                )
            )
        }
        return normalAudios
    }

    suspend fun getCustomAudio(): List<CustomAudio> {
        val customAudios = withContext(Dispatchers.IO) {
            listOf(
                CustomAudio(
                    id = 1,
                    title = "Beach",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://static.vecteezy.com/system/resources/thumbnails/012/400/885/small_2x/tropical-sunset-beach-and-sky-background-as-exotic-summer-landscape-with-beach-swing-or-hammock-and-white-sand-and-calm-sea-beach-banner-paradise-island-beach-vacation-or-summer-holiday-destination-photo.jpg",
                    volumeLevel = 20,
                    isPlaying = true
                ),
                CustomAudio(
                    id = 2,
                    title = "Fire",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://thewildlife.blog/wp-content/uploads/2023/06/daf20fe7-7922-4444-9772-8e6f79174221.jpg",
                    volumeLevel = 20,
                    isPlaying = false
                ),
                CustomAudio(
                    id = 3,
                    title = "Thunderstorm",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://www.usatoday.com/gcdn/presto/2023/06/23/USAT/4448a61c-3fad-466c-bb56-f704f34c7fd5-felix-mittermeier-Zkx_DgMQink-unsplash.jpg?crop=1436,1077,x0,y0",
                    volumeLevel = 40,
                    isPlaying = true
                ),
                CustomAudio(
                    id = 4,
                    title = "Big city",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://images.unsplash.com/photo-1515963665762-77ef90e624fa?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8YmlnJTIwY2l0eXxlbnwwfHwwfHx8MA%3D%3D",
                    volumeLevel = 20,
                    isPlaying = true
                ),
                CustomAudio(
                    id = 5,
                    title = "Bird chirping",
                    musicURL = "http://www.bogotobogo.com/Audio/sample.mp3",
                    previewImageUrl = "https://gl.audubon.org/sites/default/files/styles/article_hero_inline/public/aud_apa-2020_barn-swallow_a1-11258-1_nape_photo-xianwei-zeng.jpg?itok=hEJFNc9a",
                    volumeLevel = 50,
                    isPlaying = true
                )
            )
        }

        return customAudios.map {
            it.copy(volumeLevel = AudioSettingsManager.getAudioVolume(it.id) ?: it.volumeLevel)
        }
    }

    fun saveAudioVolume(audioId: Long, volumeLevel: Int) {
        AudioSettingsManager.setAudioVolume(audioId, volumeLevel)
    }
}