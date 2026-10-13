package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserRepositoryFake : UserRepository {

    private val _users = MutableStateFlow(
        listOf(
            User(
                id = "u1",
                name = "Anthony Edward Stark",
                email = "yosoyironman@gmail.com",
                phone = "+52 55 1234 5678",
                role = UserAccountRole.VENDEDOR,
                status = UserAccountStatus.VERIFICADO_ACTIVO,
                registrationDate = "10 oct 2023",
                activeListingsCount = 4,
                draftsCount = 1,
                avatarRes = R.drawable.prop_depto_sala
            ),
            User(
                id = "u2",
                name = "Peter Parker",
                email = "peter.parker@dailybugle.com",
                phone = "+52 55 8341 9026",
                role = UserAccountRole.COMPRADOR,
                status = UserAccountStatus.PENDIENTE_VERIFICACION,
                registrationDate = "12 oct 2024",
                activeListingsCount = 0,
                draftsCount = 0,
                avatarRes = R.drawable.prop_depto_cocina
            ),
            User(
                id = "u3",
                name = "Clark Joseph Kent",
                email = "clark.kent@dailyplanet.com",
                phone = "+1 12 2345 7862",
                role = UserAccountRole.ADMINISTRADOR,
                status = UserAccountStatus.REGISTRADO,
                registrationDate = "05 sep 2022",
                activeListingsCount = 2,
                draftsCount = 0,
                avatarRes = R.drawable.prop_casa_fachada,
                isSuperAdmin = true
            ),
            User(
                id = "u4",
                name = "Logan Howlett",
                email = "loganuwu@email.mx",
                phone = "+52 33 4455 6677",
                role = UserAccountRole.VENDEDOR,
                status = UserAccountStatus.BLOQUEADO,
                registrationDate = "15 ago 2023",
                activeListingsCount = 3,
                draftsCount = 2,
                avatarRes = R.drawable.prop_casa_sala
            ),
            User(
                id = "u5",
                name = "Bruce Thomas Wayne",
                email = "soybatman@email.mx",
                phone = "+52 55 9988 7766",
                role = UserAccountRole.COMPRADOR,
                status = UserAccountStatus.VERIFICADO_ACTIVO,
                registrationDate = "01 ene 2024",
                activeListingsCount = 0,
                draftsCount = 0,
                avatarRes = R.drawable.prop_terreno_1
            ),
            User(
                id = "u6",
                name = "Diana Prince",
                email = "diana.prince@museum.org",
                phone = "+52 55 3322 1100",
                role = UserAccountRole.VENDEDOR,
                status = UserAccountStatus.VERIFICADO_ACTIVO,
                registrationDate = "20 nov 2023",
                activeListingsCount = 5,
                draftsCount = 1,
                avatarRes = null
            ),
            User(
                id = "u7",
                name = "Barry Allen",
                email = "barry.allen@ccpd.gov",
                phone = "+52 55 1122 3344",
                role = UserAccountRole.COMPRADOR,
                status = UserAccountStatus.PENDIENTE_VERIFICACION,
                registrationDate = "03 feb 2024",
                activeListingsCount = 0,
                draftsCount = 0,
                avatarRes = null
            ),
            User(
                id = "u8",
                name = "Arthur Curry",
                email = "arthur@atlantis.oce",
                phone = "+52 66 7788 9900",
                role = UserAccountRole.VENDEDOR,
                status = UserAccountStatus.REGISTRADO,
                registrationDate = "18 jul 2024",
                activeListingsCount = 1,
                draftsCount = 3,
                avatarRes = null
            ),
            User(
                id = "u9",
                name = "Hal Jordan",
                email = "hal.jordan@testpilot.com",
                phone = "+52 55 5544 3322",
                role = UserAccountRole.COMPRADOR,
                status = UserAccountStatus.VERIFICADO_ACTIVO,
                registrationDate = "14 sep 2023",
                activeListingsCount = 0,
                draftsCount = 0,
                avatarRes = null
            ),
            User(
                id = "u10",
                name = "Natasha Romanoff",
                email = "natasha@shield.gov",
                phone = "+52 55 7777 8888",
                role = UserAccountRole.ADMINISTRADOR,
                status = UserAccountStatus.VERIFICADO_ACTIVO,
                registrationDate = "10 mar 2022",
                activeListingsCount = 0,
                draftsCount = 0,
                avatarRes = null
            ),
            User(
                id = "u11",
                name = "Wanda Maximoff",
                email = "wanda@westview.net",
                phone = "+52 55 2222 3333",
                role = UserAccountRole.VENDEDOR,
                status = UserAccountStatus.PENDIENTE_VERIFICACION,
                registrationDate = "25 sep 2024",
                activeListingsCount = 2,
                draftsCount = 0,
                avatarRes = null
            ),
            User(
                id = "u12",
                name = "Steve Rogers",
                email = "steve@firstavenger.us",
                phone = "+52 55 1941 2023",
                role = UserAccountRole.VENDEDOR,
                status = UserAccountStatus.VERIFICADO_ACTIVO,
                registrationDate = "04 jul 2023",
                activeListingsCount = 1,
                draftsCount = 0,
                avatarRes = null
            )
        )
    )

    override fun observeUsers(): Flow<List<User>> = _users.asStateFlow()

    override suspend fun getUserById(userId: String): User? =
        _users.value.firstOrNull { it.id == userId }

    override suspend fun updateUser(user: User) {
        _users.update { list ->
            list.map { if (it.id == user.id) user else it }
        }
    }

    override suspend fun deleteUser(userId: String) {
        _users.update { list ->
            list.filter { it.id != userId }
        }
    }

    override suspend fun blockUser(userId: String, reason: String?) {
        _users.update { list ->
            list.map { user ->
                if (user.id == userId) {
                    user.copy(status = UserAccountStatus.BLOQUEADO)
                } else {
                    user
                }
            }
        }
    }

    override suspend fun unblockUser(userId: String) {
        _users.update { list ->
            list.map { user ->
                if (user.id == userId) {
                    user.copy(status = UserAccountStatus.VERIFICADO_ACTIVO)
                } else {
                    user
                }
            }
        }
    }
}
