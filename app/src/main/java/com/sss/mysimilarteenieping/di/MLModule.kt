package com.sss.mysimilarteenieping.di

import android.content.Context
import com.sss.mysimilarteenieping.ml.TeeniepingClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Singleton
    @Provides
    fun provideTeeniepingClassifier(@ApplicationContext context: Context): TeeniepingClassifier {
        val classifier = TeeniepingClassifier(context)
        try {
            classifier.init()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return classifier
    }
} 