package dev.myaring.mobile_butler.data.repository

import dev.myaring.mobile_butler.data.local.ButlerDatabase

class GoalRepositoryImpl(
    database: ButlerDatabase,
) : RoomGoalRepository(database)
