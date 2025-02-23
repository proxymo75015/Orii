package com.origamilabs.orii.utils

import androidx.lifecycle.MutableLiveData

/**
 * Convertit un entier en booléen.
 * Retourne `true` si l'entier vaut 1, sinon `false`.
 */
fun Int.toBoolean(): Boolean = this == 1

/**
 * Convertit un booléen en caractère.
 * Retourne `'1'` si vrai, sinon `'0'`.
 */
fun Boolean.toChar(): Char = if (this) '1' else '0'

/**
 * Convertit un booléen en entier.
 * Retourne 1 si vrai, sinon 0.
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * Convertit un caractère représentant un chiffre en son entier correspondant.
 * Par exemple, '7' sera converti en 7.
 */
fun Char.toDigitInt(): Int = this.digitToInt()

/**
 * Convertit un entier en une chaîne binaire sur une longueur donnée, en complétant par des zéros à gauche.
 *
 * Exemple :
 * ```
 * 5.toBinaryString(8) // Retourne "00000101"
 * ```
 */
fun Int.toBinaryString(length: Int): String = this.toString(2).padStart(length, '0')

/**
 * Convertit une chaîne représentant un nombre binaire en entier.
 *
 * Par exemple, "101" sera converti en 5.
 */
fun String.toDecimalNumber(): Int = this.toInt(2)

/**
 * Extension pour initialiser une [MutableLiveData] avec une valeur par défaut.
 *
 * Exemple :
 * ```
 * val liveData = MutableLiveData<Int>().default(0)
 * ```
 */
fun <T> MutableLiveData<T>.default(initialValue: T): MutableLiveData<T> {
    value = initialValue
    return this
}
