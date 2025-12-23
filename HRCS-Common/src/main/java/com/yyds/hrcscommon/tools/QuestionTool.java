//package com.yyds.hrcscommon.tools;
//
//import dev.langchain4j.agent.tool.P;
//import dev.langchain4j.agent.tool.Tool;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;
//
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//public class QuestionTool {
//    @Tool(name = "interviewQuestionSearch", value = """
//               Retrieves relevant interview questions from mianshiya.com based on a keyword.
//               Use this tool when the user asks for interview questions about specific technologies,
//               programming concepts, or job-related topics. The input should be a clear search term.
//               """
//    )
//    public String searchInterviewQuestions(@P(value = "the keyword to search") String keyword) {
//        List<String> questions = new ArrayList<>();
//        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
//        String url = "https://www.mianshiya.com/search/all?searchText=" + encodedKeyword;
//
//        Document doc;
//        try {
//            doc = Jsoup.connect(url)
//                    .userAgent("Mozilla/5.0")
//                    .timeout(5000)
//                    .get();
//        } catch (IOException e) {
//            log.error("get web error", e);
//            return e.getMessage();
//        }
//
//        Elements questionElements = doc.select(".ant-table-cell > a");
//        questionElements.forEach(el -> questions.add(el.text().trim()));
//        return String.join("\n", questions);
//    }
//}
   