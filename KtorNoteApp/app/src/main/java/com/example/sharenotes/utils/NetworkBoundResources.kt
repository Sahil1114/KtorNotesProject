package com.example.sharenotes.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.*

inline fun <ResultType,RequestType> networkBoundResources(
    crossinline query:() ->Flow<ResultType>,
    crossinline fetch :suspend ()->RequestType,
    crossinline saveFetchResult:suspend (RequestType)->Unit,
    crossinline onFetchFailed:(Throwable)->Unit={Unit},
    crossinline shouldFetch:(ResultType)->Boolean={true}
)= flow {
    emit(Resource.Loading(null))
    val data=query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading(data))

        try {
            val fetchedResult=fetch()
            saveFetchResult(fetchedResult)
            query().map { Resource.Success(it) }
        }catch (t :Throwable){
            onFetchFailed(t)
            query().map { Resource.Error(it,"Couldn't reach server.It might be down") }
        }
    } else {
        query().map { Resource.Success(it) }
    }
    emitAll(flow)
}