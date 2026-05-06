package com.example.fitgen.domain.model

/**
 * Merepresentasikan satu gerakan latihan dalam sebuah sesi workout.
 *
 * @property nama   Nama gerakan, misal "Push Up", "Squat", "Deadlift".
 * @property sets   Jumlah set yang dilakukan.
 * @property reps   Jumlah repetisi per set.
 * @property beban  Beban dalam kilogram (0.0 jika gerakan tanpa beban).
 */
data class Exercise(
    val nama: String,
    val sets: Int,
    val reps: Int,
    val beban: Double = 0.0
) {
    /** Label ringkas untuk ditampilkan di UI, misal "3 x 12 @ 50 kg". */
    val label: String
        get() = if (beban > 0.0) "$sets x $reps @ $beban kg" else "$sets x $reps"

    /** Validasi sederhana – gerakan dianggap valid jika nama tidak kosong dan sets/reps > 0. */
    val isValid: Boolean
        get() = nama.isNotBlank() && sets > 0 && reps > 0
}
