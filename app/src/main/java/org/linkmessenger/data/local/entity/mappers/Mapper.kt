package org.linkmessenger.data.local.entity.mappers

interface Mapper<SRC, DST> {
    fun transform(data: SRC): DST
}