package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppRepository(
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val messageDao: MessageDao
) {
    // Current User
    val currentUser: Flow<User?> = userDao.getCurrentUserFlow()

    suspend fun getLoggedInUserSync(): User? = userDao.getCurrentUser()

    suspend fun login(phoneNumber: String, name: String) {
        userDao.loginUser(phoneNumber, name)
    }

    suspend fun logout() {
        userDao.logout()
    }

    // Profiles
    val allProfiles: Flow<List<Profile>> = profileDao.getAllProfiles()

    fun getProfilesByCategory(category: String): Flow<List<Profile>> {
        return profileDao.getProfilesByCategory(category)
    }

    fun getProfileById(id: Int): Flow<Profile?> {
        return profileDao.getProfileById(id)
    }

    suspend fun insertProfile(profile: Profile) {
        profileDao.insertProfile(profile)
    }

    fun searchProfiles(query: String): Flow<List<Profile>> {
        return profileDao.searchProfiles(query)
    }

    // Community Posts
    val allPosts: Flow<List<CommunityPost>> = postDao.getAllPosts()

    suspend fun insertPost(post: CommunityPost) {
        postDao.insertPost(post)
    }

    // Comments
    fun getCommentsForPost(postId: Int): Flow<List<Comment>> {
        return commentDao.getCommentsForPost(postId)
    }

    suspend fun insertComment(comment: Comment) {
        commentDao.insertComment(comment)
    }

    // Messages / Chat
    fun getMessagesWith(myPhone: String, peerPhone: String): Flow<List<Message>> {
        return messageDao.getMessagesWith(myPhone, peerPhone)
    }

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
    }

    // Retrieve active chats grouped by user
    fun getRecentChats(myPhone: String): Flow<List<ChatPreview>> {
        return messageDao.getAllConversationsRaw(myPhone).map { messages ->
            messages.groupBy { msg ->
                if (msg.senderPhone == myPhone) msg.receiverPhone else msg.senderPhone
            }.map { (peerPhone, msgs) ->
                val latest = msgs.maxByOrNull { it.timestamp }!!
                val peerName = if (latest.senderPhone == myPhone) latest.receiverName else latest.senderName
                ChatPreview(
                    peerPhone = peerPhone,
                    peerName = peerName,
                    lastMessage = latest.text,
                    timestamp = latest.timestamp
                )
            }.sortedByDescending { it.timestamp }
        }
    }

    suspend fun prepopulateIfNeeded() {
        // Run check
        val currentProfiles = profileDao.getAllProfiles().first()
        if (currentProfiles.isEmpty()) {
            val defaults = listOf(
                Profile(
                    category = "مطاعم",
                    name = "مطعم أبو أنس السوري",
                    title = "شاورما، مشويات، طواجن سورية ومصرية",
                    description = "أشهى المأكولات السورية والمشويات على الفحم في المنصورية. شاورما لحم وفراخ، كبسة، كباب، خدمة توصيل للمنازل متميزة وتلبية الطلبات والحفلات.",
                    phoneNumber = "01023456789",
                    mapsLink = "https://maps.app.goo.gl/AbuAnasDummyLink",
                    logoUri = "rest_abu_anas",
                    extraImages = "rest_1,rest_2,rest_3",
                    ownerPhone = "01023456789"
                ),
                Profile(
                    category = "قطع غيار ومواتير",
                    name = "المنصورية لقطع غيار ومواتير المياه",
                    title = "مواتير مياه إيطالية، قطع غيار سيارات وأدوات زراعية",
                    description = "جميع أنواع مواتير المياه الإلترو صينية وإيطالية، قطع غيار أصلية ومستوردة، خراطيم مياه متينة، طلمبات زراعية ومولدات كهرباء عالية الجودة وعقد صيانة للورش والمزارع بالمنصورية.",
                    phoneNumber = "01145678901",
                    mapsLink = "https://maps.app.goo.gl/PartsMotorsDummyLink",
                    logoUri = "shop_motors",
                    extraImages = "motor_1,motor_2",
                    ownerPhone = "01145678901"
                ),
                Profile(
                    category = "أدوات كهربائية",
                    name = "الشركة الهندسية للتوريدات واللوحات",
                    title = "لوحات كهرباء، مفاتيح تلامس، كابلات وأسلاك السويدي",
                    description = "نبيع خامات ومستلزمات لوحات التحكم الكهربائية، كابلات السويدي الأصلية النحاس، مفاتيح اتوماتيك شنايدر، لمبات ليد، مفاتيح إنارة، وفيوزات بجودة عالية للمحلات والمنازل.",
                    phoneNumber = "01234567890",
                    mapsLink = "https://maps.app.goo.gl/EngSuppliesDummyLink",
                    logoUri = "shop_electrical",
                    extraImages = "elec_1,elec_2",
                    ownerPhone = "01234567890"
                ),
                Profile(
                    category = "مهندسين وفنيين",
                    name = "مهندس شريف كمال - كهرباء تحكم آلي",
                    title = "مهندس تصميم وتجميع لوحات كهربائية PLC & Classic Control",
                    description = "تصميم وتنفيذ لوحات التحكم الآلي للمصانع والورش ومحطات المياه بالمنصورية والجيزة. صيانة لوحات الكهرباء وحل جميع أعطال الصيانة الصناعية وبرمجة الشاشات وأجهزة الانفرتر.",
                    phoneNumber = "01511223344",
                    mapsLink = "https://maps.app.goo.gl/EngSherifDummyLink",
                    logoUri = "eng_sherif",
                    extraImages = "eng_1,eng_2",
                    ownerPhone = "01511223344"
                ),
                Profile(
                    category = "محلات وورش",
                    name = "براغي ومسامير المنصورية",
                    title = "محل مسامير، أدوات ربط، عدد يدوية ومستلزمات حدادة",
                    description = "نوفر تشكيلة واسعة جداً من المسامير بجميع الأحجام (قلاووظ، براغي، صواميل، تيل، مسامير خشب وجبس، خابور) بالإضافة للعدد والأدوات اليدوية للورش والمهندسين وأصحاب المحلات.",
                    phoneNumber = "01099887766",
                    mapsLink = "https://maps.app.goo.gl/ScrewsDummyLink",
                    logoUri = "shop_screws",
                    extraImages = "screws_1,screws_2",
                    ownerPhone = "01099887766"
                ),
                Profile(
                    category = "أطباء ومستشفيات",
                    name = "عيادات مستشفى المنصورية التخصصي",
                    title = "طوارئ ٢٤ ساعة، عيادات تخصصية وصيدلية متكاملة",
                    description = "رعاية طيبة متكاملة وطاقم طبي متميز في تخصصات عيادات الأطفال، الباطنة وقسم القلب، النساء والتوليد، الجراحة العامة. قسم طوارئ واستقبال مجهز بالكامل على مدار الساعة.",
                    phoneNumber = "01122334455",
                    mapsLink = "https://maps.app.goo.gl/HospDummyLink",
                    logoUri = "hosp_mansouria",
                    extraImages = "hosp_1,hosp_2",
                    ownerPhone = "01122334455"
                ),
                Profile(
                    category = "سكن وعقارات",
                    name = "مكتب المنصورية للتسويق العقاري",
                    title = "شقق مستقلة وعائلية مفروشة للإيجار، أراضي ومحلات تجارية",
                    description = "نوفر شقق سكنية مفروشة وغير مفروشة للإيجار الشهري، استوديوهات، مكاتب إدارية، ومحلات تجارية للبيع والإيجار في أفضل مواقع المنصورية الحيوية والقريبة من المواصلات.",
                    phoneNumber = "01288776655",
                    mapsLink = "https://maps.app.goo.gl/PropertyDummyLink",
                    logoUri = "prop_mansouria",
                    extraImages = "prop_1",
                    ownerPhone = "01288776655"
                ),
                Profile(
                    category = "مهندسين وفنيين",
                    name = "الأسطواني صبري - فني لوحات وتأسيس كهرباء",
                    title = "فني تمديدات وتأسيس إضاءة وشبكات كهربائية منزلية",
                    description = "تأسيس وتشتشيب شقق وفيلات ومحلات بجودة ممتازة، صيانة أعطال الكهرباء المنزلية واللوحات الفرعية، تأسيس خطوط التكييف، تركيب إضاءات ليد مخفية وإنارة حدائق بأمان تام وبأسعار مناسبة.",
                    phoneNumber = "01055443322",
                    mapsLink = "https://maps.app.goo.gl/SabryElectricDummyLink",
                    logoUri = "tech_sabry",
                    extraImages = "elec_3",
                    ownerPhone = "01055443322"
                )
            )
            defaults.forEach { profileDao.insertProfile(it) }
        }

        // Add some default community posts
        val currentPosts = postDao.getAllPosts().first()
        if (currentPosts.isEmpty()) {
            val defaultPosts = listOf(
                CommunityPost(
                    authorName = "أحمد يسري",
                    authorPhone = "01123432123",
                    content = "يا جماعة لو سمحتم، مين يعرف محل في المنصورية بيبيع مسامير قلاووظ سن طويل وجبس؟ محتاج كمية للورشة ويكون شغال دلوقتي."
                ),
                CommunityPost(
                    authorName = "أم محمد",
                    authorPhone = "01056765432",
                    content = "لو سمحتوا بدور على دكتور أطفال شاطر وموجود بالليل ضروري في المنصورية، بنتي عندها سخونية والوقت متأخر."
                )
            )
            defaultPosts.forEach { postDao.insertPost(it) }

            // Add initial comments linking actual profiles
            val savedPosts = postDao.getAllPosts().first()
            if (savedPosts.isNotEmpty()) {
                val postWithScrews = savedPosts.first { it.content.contains("مسامير") }
                commentDao.insertComment(
                    Comment(
                        postId = postWithScrews.id,
                        authorName = "محمد صابر (محل البراغي)",
                        authorPhone = "01099887766",
                        linkedProfileId = 5, // ID 5 corresponds to "براغي وممسامير المنصورية"
                        content = "أهلاً يا أستاذ أحمد، إحنا شغالين حالياً في محل براغي ومسامير المنصورية، وعندنا كل المقاسات اللي طالبها ونقدر نجهزلك الكمية فوراً. ده رابط صفحتنا."
                    )
                )

                val postWithDoctor = savedPosts.first { it.content.contains("دكتور") }
                commentDao.insertComment(
                    Comment(
                        postId = postWithDoctor.id,
                        authorName = "مستشفى المنصورية",
                        authorPhone = "01122334455",
                        linkedProfileId = 6, // Clinis/Hosp profile
                        content = "ألف سلامة عليها يا فندم. قسم الطوارئ والاستقبال واستشاري الأطفال متواجدين ٢٤ ساعة بمستشفى المنصورية التخصصي بوسط البلد بجوار المسجد الكبير."
                    )
                )
            }
        }
    }
}

data class ChatPreview(
    val peerPhone: String,
    val peerName: String,
    val lastMessage: String,
    val timestamp: Long
)
