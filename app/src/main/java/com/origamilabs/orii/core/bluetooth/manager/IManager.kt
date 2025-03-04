package com.origamilabs.orii.core.bluetooth.manager

/**
 * Interface définissant les méthodes de base pour un manager.
 */
interface IManager {
    fun initialize(): Boolean
    fun start()
    fun close()
}
