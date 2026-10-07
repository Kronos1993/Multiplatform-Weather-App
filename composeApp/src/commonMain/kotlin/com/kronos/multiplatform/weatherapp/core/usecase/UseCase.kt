package com.kronos.multiplatform.weatherapp.core.usecase

abstract class UseCase<in P, out R> {
    abstract suspend fun run(params: P): R

    suspend operator fun invoke(params: P): R = run(params)
}
