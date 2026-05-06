package com.example.fitgen.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Merepresentasikan satu sesi log latihan yang dilakukan oleh pengguna.
 *
 * @property id          ID unik log (0 berarti belum disimpan ke database).
 * @property tanggal     Waktu/tanggal sesi latihan dicatat (default: sekarang).
 * @property daftarGerakan Daftar [Exercise] yang dilakukan dalam sesi ini.
 */
data class WorkoutLog(
    val id: Long = 0,
    val tanggal: Instant = Clock.System.now(),
    val daftarGerakan: List<Exercise> = emptyList()
) {
    /** Total jumlah gerakan dalam sesi ini. */
    val totalGerakan: Int
        get() = daftarGerakan.size

    /** Total volume beban (sets × reps × beban) seluruh gerakan, dalam kg. */
    val totalVolume: Double
        get() = daftarGerakan.sumOf { it.sets * it.reps * it.beban }

    /** Apakah log ini memiliki minimal satu gerakan. */
    val isEmpty: Boolean
        get() = daftarGerakan.isEmpty()
}
