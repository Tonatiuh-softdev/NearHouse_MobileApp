package com.equipo4.nearhome.ui.legal

data class LegalSection(
    val id: Int,
    val title: String,
    val content: String
)

data class LegalDocumentData(
    val title: String,
    val lastUpdated: String,
    val subtitle: String,
    val sections: List<LegalSection>
)