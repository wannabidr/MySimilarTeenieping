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
@InstallIn(SingletonComponent::class) // 애플리케이션 생명주기 동안 싱글톤으로 제공
object MLModule {

    @Singleton
    @Provides
    fun provideTeeniepingClassifier(@ApplicationContext context: Context): TeeniepingClassifier {
        // 모델 및 레이블 파일명은 TeeniepingClassifier의 기본값을 사용합니다.
        // 필요시 여기서 파일명을 직접 지정할 수도 있습니다.
        return TeeniepingClassifier(context)
    }
} 