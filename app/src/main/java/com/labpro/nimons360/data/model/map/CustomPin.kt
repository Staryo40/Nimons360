package com.labpro.nimons360.data.model.map

data class CustomPin(
    val id: String,
    val label: String,
    val url: String,
) {
    companion object {
        val all = listOf(
            CustomPin("lizard", "Lizard", "https://mad.labpro.hmif.dev/assets/pin/lizard.png"),
            CustomPin("moon", "Moon", "https://mad.labpro.hmif.dev/assets/pin/moon.png"),
            CustomPin("redpin", "Pin Point", "https://mad.labpro.hmif.dev/assets/pin/redpin.png"),
            CustomPin("smile", "Smile", "https://mad.labpro.hmif.dev/assets/pin/smile.png"),
            CustomPin("star", "Star", "https://mad.labpro.hmif.dev/assets/pin/star.png"),
        )

        fun find(id: String?): CustomPin? = all.firstOrNull { it.id == id }
    }
}
