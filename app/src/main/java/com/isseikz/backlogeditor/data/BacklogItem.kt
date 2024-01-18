package com.isseikz.backlogeditor.data

data class BacklogItem(
    val id: String,
    val title: String,
    val status: BacklogStatus,
    val priority: Int
) {
    override fun toString(): String {
        return "BacklogItem(id='$id', title='$title', status=$status, priority=$priority)"
    }
}
