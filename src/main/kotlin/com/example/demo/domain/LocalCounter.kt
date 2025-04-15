package com.example.demo.domain

import com.example.demo.infra.CounterSessionManager

/**
 * Grow-Only Counter(https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type#G-Counter_(Grow-only_Counter))에서
 * 착안하여 만든 CRDT 구현체. 한 사용자마다 10개의 카운터가 있고 다른 사용자와 통신 없이 로컬에서 카운터를 증가시킬 수 있다고 할 때
 * 사용자 로컬의 카운터를 표현하기 위한 클래스. 실제로 동작하려면 이 사용자 별 카운터를 전체 상태로 갖고있어야 할 것.
 *
 * payload integer\[n\] P
 *     initial [0,0,...,0]
 * update increment()
 *     let g = myId()
 *     P[g] := P[g] + 1
 * query value() : integer v
 *     let v = Σi P[i]
 * compare (X, Y) : boolean b
 *     let b = (∀i ∈ [0, n - 1] : X.P[i] ≤ Y.P[i])
 * merge (X, Y) : payload Z
 *     let ∀i ∈ [0, n - 1] : Z.P[i] = max(X.P[i], Y.P[i])
 */
class LocalCounter(
    /** payload */
    val values: IntArray,
    /** client unique id */
    val uid: String,
) {

    /** 현재 인스턴스가 파라미터로 받은 인스턴스보다 monotone 하게 최신인지? */
    fun compare(other: LocalCounter): Boolean {
        return values.indices.all { values[it] >= other.values[it] }
    }

    /** 현재 인스턴스의 카운터를 다른 카운터와 병합한다. Grow-Only 카운터기 때문에 maxOf로 monotone 유지 가능. */
    fun merge(other: LocalCounter): LocalCounter {
        val merged = IntArray(values.size)
        synchronized(CounterSessionManager.COUNTER) {
            for (i in values.indices) {
                merged[i] = maxOf(values[i], other.values[i])
            }
        }
        return LocalCounter(merged, uid)
    }
}