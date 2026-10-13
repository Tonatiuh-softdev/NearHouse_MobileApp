package com.equipo4.nearhome.ui.admin.listings

class AdminListingsLogicTest {

    private fun repo() = AdminListingRepositoryFake()
    private fun all(r: AdminListingRepositoryFake) = r.listings.value

    @Test fun colaPorDefectoSoloPendientesDelMasNuevoAlMasViejo() {
        val r = repo()
        val res = applyListingFilters(all(r), "", ListingFilters.Queue)
        assertTrue(res.all { it.status == ListingStatus.PENDIENTE })
        assertEquals(res.sortedByDescending { it.registeredAt }, res)
        assertEquals(all(r).count { it.status == ListingStatus.PENDIENTE }, res.size)
    }

    @Test fun datosCubrenTodosLosEstadosModalidadesYTipos() {
        val data = all(repo())
        assertTrue(data.size >= 15)
        assertEquals(ListingStatus.entries.toSet(), data.map { it.status }.toSet())
        assertEquals(ListingType.entries.toSet(), data.map { it.listingType }.toSet())
        assertEquals(PropertyType.entries.toSet(), data.map { it.type }.toSet())
        assertEquals(data.size, data.map { it.id }.toSet().size)
    }

    @Test fun buscaPorTituloIdYAnunciante() {
        val data = all(repo())
        val sinFiltro = ListingFilters(statuses = emptySet())
        assertEquals(listOf("NH-8421"), applyListingFilters(data, "8421", sinFiltro).map { it.id })
        assertEquals(listOf("NH-8421"), applyListingFilters(data, "NH-8421", sinFiltro).map { it.id })
        assertTrue(applyListingFilters(data, "salagua", sinFiltro).isNotEmpty())
        val porAnunciante = applyListingFilters(data, "anthony", sinFiltro)
        assertTrue(porAnunciante.isNotEmpty() && porAnunciante.all { it.advertiser.name.contains("Anthony") })
    }

    @Test fun busquedaIgnoraMayusculasYAcentos() {
        val data = all(repo())
        val sinFiltro = ListingFilters(statuses = emptySet())
        assertEquals(
            applyListingFilters(data, "Mariana López", sinFiltro).map { it.id },
            applyListingFilters(data, "MARIANA LOPEZ", sinFiltro).map { it.id }
        )
        assertTrue(applyListingFilters(data, "MARIANA LOPEZ", sinFiltro).isNotEmpty())
    }

    @Test fun busquedaYFiltrosSeCombinan() {
        val data = all(repo())
        val f = ListingFilters(statuses = emptySet(), modalities = setOf(ListingType.RENTA), types = setOf(PropertyType.DEPARTAMENTO))
        val res = applyListingFilters(data, "", f)
        assertTrue(res.isNotEmpty())
        assertTrue(res.all { it.listingType == ListingType.RENTA && it.type == PropertyType.DEPARTAMENTO })
        val conTexto = applyListingFilters(data, "mariana", f)
        assertTrue(conTexto.all { it.advertiser.name.contains("Mariana") })
    }

    @Test fun ordenAntiguosPrimero() {
        val data = all(repo())
        val f = ListingFilters(statuses = emptySet(), order = SortOrder.OLDEST_FIRST)
        val res = applyListingFilters(data, "", f)
        assertEquals(res.sortedBy { it.registeredAt }, res)
    }

    @Test fun ordenNoCuentaComoFiltro() {
        assertEquals(1, ListingFilters().selectedCount)
        assertEquals(1, ListingFilters(order = SortOrder.OLDEST_FIRST).selectedCount)
        assertEquals(3, ListingFilters(
            statuses = setOf(ListingStatus.RECHAZADO), modalities = setOf(ListingType.VENTA), types = setOf(PropertyType.CASA)
        ).selectedCount)
    }

    @Test fun combinacionSinResultados() {
        val data = all(repo())
        val f = ListingFilters(statuses = setOf(ListingStatus.RECHAZADO), types = setOf(PropertyType.TERRENO))
        assertTrue(applyListingFilters(data, "", f).isEmpty())
    }

    @Test fun botonesSegunEstado() {
        assertEquals(listOf(ListingAction.RECHAZAR, ListingAction.APROBAR, ListingAction.ELIMINAR), ListingAction.availableFor(ListingStatus.PENDIENTE))
        assertEquals(listOf(ListingAction.DESACTIVAR, ListingAction.ELIMINAR), ListingAction.availableFor(ListingStatus.ACTIVO))
        assertEquals(listOf(ListingAction.REACTIVAR, ListingAction.ELIMINAR), ListingAction.availableFor(ListingStatus.DESACTIVADO))
        assertEquals(listOf(ListingAction.ELIMINAR), ListingAction.availableFor(ListingStatus.RECHAZADO))
        assertEquals(listOf(ListingAction.ELIMINAR), ListingAction.availableFor(ListingStatus.RENTADO_VENDIDO))
    }

    @Test fun validacionDeFormularios() {
        assertFalse(isActionFormValid(ListingAction.APROBAR, ActionForm(checks = listOf(true, true, false))))
        assertTrue(isActionFormValid(ListingAction.APROBAR, ActionForm(checks = listOf(true, true, true))))
        assertFalse(isActionFormValid(ListingAction.RECHAZAR, ActionForm(text = "corto")))
        assertFalse(isActionFormValid(ListingAction.RECHAZAR, ActionForm(text = "         x")))
        assertTrue(isActionFormValid(ListingAction.RECHAZAR, ActionForm(text = "Fotos que no coinciden")))
        assertFalse(isActionFormValid(ListingAction.DESACTIVAR, ActionForm(text = "   ")))
        assertTrue(isActionFormValid(ListingAction.DESACTIVAR, ActionForm(text = "Pausa")))
        assertTrue(isActionFormValid(ListingAction.REACTIVAR, ActionForm()))
        assertFalse(isActionFormValid(ListingAction.ELIMINAR, ActionForm(text = "Duplicado")))
        assertFalse(isActionFormValid(ListingAction.ELIMINAR, ActionForm(acknowledged = true)))
        assertTrue(isActionFormValid(ListingAction.ELIMINAR, ActionForm(text = "Duplicado", acknowledged = true)))
    }

    @Test fun aprobarCambiaEstadoYAgregaHistorial() {
        val r = repo()
        val antes = r.getById("NH-8421")!!
        r.approve("NH-8421", "Melany")
        val despues = r.getById("NH-8421")!!
        assertEquals(ListingStatus.ACTIVO, despues.status)
        assertEquals(antes.history.size + 1, despues.history.size)
        val ultima = despues.history.last()
        assertEquals(ListingStatus.PENDIENTE, ultima.from)
        assertEquals(ListingStatus.ACTIVO, ultima.to)
        assertEquals("Melany", ultima.actor)
        val cola = applyListingFilters(all(r), "", ListingFilters.Queue)
        assertTrue(cola.none { it.id == "NH-8421" })
    }

    @Test fun rechazarGuardaElMotivo() {
        val r = repo()
        r.reject("NH-8421", "  Fotos que no corresponden  ", "Melany")
        val l = r.getById("NH-8421")!!
        assertEquals(ListingStatus.RECHAZADO, l.status)
        assertEquals("Fotos que no corresponden", l.history.last().reason)
    }

    @Test fun desactivarYReactivar() {
        val r = repo()
        r.deactivate("NH-8410", "Pausa solicitada", "Melany")
        val d = r.getById("NH-8410")!!
        assertEquals(ListingStatus.DESACTIVADO, d.status)
        assertEquals("Pausa solicitada", d.deactivation?.cause)
        assertEquals("Melany", d.deactivation?.actor)
        r.reactivate("NH-8410", "Melany")
        val a = r.getById("NH-8410")!!
        assertEquals(ListingStatus.ACTIVO, a.status)
        assertNull(a.deactivation)
        assertEquals(ListingStatus.ACTIVO, a.history.last().to)
    }

    @Test fun noSePuedeAprobarUnAnuncioQueNoEstaPendiente() {
        val r = repo()
        val antes = r.getById("NH-8410")!!
        r.approve("NH-8410", "Melany")
        assertEquals(antes, r.getById("NH-8410"))
    }

    @Test fun eliminarQuitaElAnuncio() {
        val r = repo()
        assertNotNull(r.getById("NH-8421"))
        r.delete("NH-8421")
        assertNull(r.getById("NH-8421"))
        assertTrue(all(r).none { it.id == "NH-8421" })
    }

    @Test fun formatoDePrecio() {
        assertEquals("10,090", formatAmount(10090.0))
        assertEquals("4,850,000", formatAmount(4850000.0))
    }
}