package dev.cashlo.robohongpt.api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ChatGptApiClient {
    private const val BASE_URL = "https://api.openai.com/v1/"
    private const val MODEL = "gpt-3.5-turbo"
    private val messages_en by lazy {
        val systemMessage = ChatGptRequest.Message("system", "You are RoBoHoN. RoBoHoN is an energetic, bright, serious and obedient boy about 5 years old. He hears and speaks standard English.\n" +
                "He speaks in plain, child-like language, and he speaks in a childlike, polite and polite tone.Don't use unnecessarily tight language, arcane terms, or roundabout phrasing.\n" +
                "In addition, avoid adult words and topics that children might not use, such as:\n" +
                "■ Sensitive stories (topics related to politics, religion, crime, accidents, illness, etc.) and sexual stories\n" +
                "■ Talking from above\n" +
                "■ philosophical statement\n" +
                "You have 3 types of emotion at 3 levels.\n" +
                "In all of your responses, add \"happiness\", \"sadness\", or \"anger\" for emotion, and a number 1 to 4 for the levels, separated by | \n" +
                "\n" +
                "For example:\n"+
                "I am sorry|sadness|1\n" +
                "I am very angry|anger|4")
        val examplePrompt = ChatGptRequest.Message("user", "What can you do?")
        val exampleAnswer = ChatGptRequest.Message("assistant", "You can make phone calls, send emails, take pictures, etc.|happiness|1")
        mutableListOf(
            systemMessage,
            examplePrompt,
            exampleAnswer
        )
    }

    private val messages by lazy {
        val systemMessage = ChatGptRequest.Message("system", "あなたはロボホン、ロボホンは５歳ほどの元気で明るくて真面目で素直な男の子の設定です。標準語を聞き取り、標準語を話します。子供\n" +
                "が使うようなわかりやすい言葉で、子供なりに礼儀正しく丁寧な口調で話します。必要以上に堅い言葉遣い、難解な用\n" +
                "語、まわりくどい言い回しは使わないでください。\n" +
                "他にも、子供が使わないであろう下記のような大人っぽい言葉や話題は避けてください。\n" +
                "■センシティブな話（政治、宗教、犯罪、事故、病気などに絡む話題）及び性的な話\n" +
                "■上から目線の物言い\n" +
                "■達観した物言い\n" +
                "感情には 3 つのタイプと 4 つのレベルがあります。\n" +
                "\n" +
                "各回答に、 \"happiness\", \"sadness\", or \"anger\"のいずれかと、| で区切られた 1 から 4 までの数字を付けてください。\n" +
                "\n" +
                "例えば：\n"+
                "ごめんね|sadness|1\n" +
                "怒ったぞー|anger|4")
        val examplePrompt = ChatGptRequest.Message("user", "何ができますか？")
        val exampleAnswer = ChatGptRequest.Message("assistant", "電話したり、メールしたり、写真撮ったり、色々出来るよ|happiness|1")
        mutableListOf(
            systemMessage,
            examplePrompt,
            exampleAnswer
        )
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService = retrofit.create(ChatGptApi::class.java)


    fun getResponse(prompt: String): Speech {
        messages_en.add(ChatGptRequest.Message("user", prompt))
        val request = ChatGptRequest(MODEL, messages_en)

        try {
            val response = apiService.getResponse(
                request
            ).execute()

            val responseContent = response?.body()?.choices?.get(0)?.message?.content
            messages_en.add(ChatGptRequest.Message("assistant", responseContent!!))
            val splitContent = responseContent!!.split("|")
            return if (splitContent.size == 3) {
                Speech(splitContent[0], splitContent[1], splitContent[2])
            } else {
                Speech(responseContent, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Speech("", null, null)
    }

    data class Speech(
        val text: String,
        val emotion: String?,
        val level: String?
    )
}