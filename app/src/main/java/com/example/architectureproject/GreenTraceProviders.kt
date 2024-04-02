package com.example.architectureproject

import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.community.FirebaseCommunityManager
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.BasicTrackingImpactProvider
import com.example.architectureproject.tracking.FirebaseTrackingDataProvider
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.demo.DummyMapProvider

object GreenTraceProviders {
    lateinit var userProvider: UserProvider
        private set
    lateinit var communityManager: CommunityManager
        private set
    val mapProvider: MapProvider = DummyMapProvider()
    val impactProvider: TrackingImpactProvider = BasicTrackingImpactProvider()

    lateinit var trackingProvider: TrackingDataProvider
        private set
    suspend fun init() {
        initUserProvider()
    }

    private suspend fun initUserProvider() {
        if (this::userProvider.isInitialized) return
        userProvider = FirebaseUserProvider.new()
    }

    suspend fun initTracking() {
        if (this::trackingProvider.isInitialized) return
        communityManager = FirebaseCommunityManager()
        trackingProvider = FirebaseTrackingDataProvider()

        // FIXME: remove demo hack
        //DummyTrackingData().addTo(trackingProvider!!)
//        trackingProvider!!.addActivity(
//            Transportation(
//                ZonedDateTime.ofInstant(
//                Instant.ofEpochSecond(1679723785),
//                ZoneId.of("America/Toronto")
//            ), "Commute 2",
//            listOf(
//                Transportation.Stop("Home", 43.57588850506251, -79.61096747795044),
//                Transportation.Stop("GO Station", 43.595679311751454, -79.64867577582866)
//            ), Transportation.Mode.Car, RecurrenceSchedule(
//                TrackingDataGranularity.Week, 2
//            )
//            )
//        )

        /*listOf(
            CommunityChallenge(
                "Plant a tree",
                "",
                20f,
                // average lifespan of 400 years, 48 lbs of CO2 per tree per year, 20 trees
                21.7724e3f * 20 * 400
            ),
            CommunityChallenge(
                "Build a rain collection system with your neighbours",
                "A rain collection system such as a rain barrel connected to the gutter system can collect up hundreds of liters of water per storm. This water can be used for watering plants, lawncare, or cleaning.",
                2f,
                // average rain barrel can save 1300 gallons a year
                // 1000 liters of water consumed produce 10.6kg of CO2 due to transport/treatment
                4921f / 1000 * 10.6e3f * 2
            ),
            CommunityChallenge(
                "Test3",
                "I don't know",
                10f,
                432e3f
            ),
            CommunityChallenge(
                "Test3",
                "I don't know",
                10f,
                432e3f
            ),
            CommunityChallenge(
                "Test4",
                "Description4",
                40f,
                432e3f
            ),
            CommunityChallenge(
                "Test5",
                "Description5",
                40f,
                432e3f
            ),
            CommunityChallenge(
                "blah blah blah",
                "there's not much to say",
                10f,
                432e3f
            ),
            CommunityChallenge(
                "what else can I put here?",
                "I'm bored",
                10f,
                432e3f
            ),
            CommunityChallenge(
                "Too Dead to Care - A Lifetime of Consequences",
                "The ragged old man coughed and gasped for breath. " +
                        "As the life giving air entered his lungs, a gurgling, wheezing sound was heard. " +
                        "His days would be numbered. As he looked out of his 20th floor apartment window, the luminous crescent moon, tainted yellow by the toxic air, hung in the lonely night sky. " +
                        "The moonlight illuminated the gray, cracked exteriors of the buildings below, surrounded by a sea of destruction. " +
                        "Slowly, the light faded, as dark clouds on the distant horizon inched closer. " +
                        "What once was a brilliant city skyline, filled with high-rise buildings, had been reduced to nothing but a shadow of its former glory. " +
                        "The energy crisis of 2060 brought about a predicable reaction from the world's leaders. " +
                        "And as war ravaged the once beautiful planet, the crisis was exacerbated, forming a vicious cycle of despair and chaos. " +
                        "The sound of thunder in the distance shook him from his melancholy reverie. " +
                        "The raindrops splashing in from the window mixed with his teardrops, dripping onto the rusty surface before him. " +
                        "Reminiscing always made him sad; after all, nothing will ever be the same again. " +
                        "Getting up, he cursed as he hit his head on a shelf, a nasty bump forming immediately. " +
                        "Tired and cranky, he prepared for bed. " +
                        "As he finally fell asleep, pleasant memories of his youth filled his dreams. " +
                        "His sister was laughing and singing the archaic children's song:\n" +
                        "It's raining, it's pouring\n" +
                        "The old man is snoring\n" +
                        "He went to bed with a bump on his head\n" +
                        "And couldn't get up in the morning",
                 1f,
                2075f
            ),
            CommunityChallenge(
                "Pick up pieces of litter from the street",
                "",
                100f,
                10e3f
            )).forEach { communityManager.addCommunityChallenge(it) }*/
    }
}