package com.hyperrecursion.home_screen_vault2.widget

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WidgetStateDataStoreModule {
    private const val DATA_STORE_FILE_NAME = "widget_states.pb"

    @Provides
    @Singleton
    fun provideWidgetStateDataStore(
        @ApplicationContext context: Context
    ): DataStore<WidgetStateProto> {
        Log.d("WidgetStateDataStoreModule", context.dataDir.path)
        return DataStoreFactory.create(
            produceFile = { java.io.File("${context.dataDir.path}/$DATA_STORE_FILE_NAME") },
            serializer = WidgetStateProtoSerializer
        )
    }
}

object WidgetStateProtoSerializer : Serializer<WidgetStateProto> {

    override val defaultValue: WidgetStateProto = WidgetStateProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): WidgetStateProto {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext WidgetStateProto.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }
    }

    override suspend fun writeTo(
        t: WidgetStateProto,
        output: OutputStream
    ) {
        withContext(Dispatchers.IO) {
            t.writeTo(output)
        }
    }
}