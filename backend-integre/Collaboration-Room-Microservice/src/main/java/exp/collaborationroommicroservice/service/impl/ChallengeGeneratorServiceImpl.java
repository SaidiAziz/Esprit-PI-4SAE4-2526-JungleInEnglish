package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.CreateChallengeRequest;
import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.service.ChallengeGeneratorService;
import exp.collaborationroommicroservice.service.ChallengeService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChallengeGeneratorServiceImpl implements ChallengeGeneratorService {

    private final RoomService roomService;
    private final ChallengeService challengeService;

    @Override
    public LinguisticChallenge generateForRoom(Long roomId, Long currentUserId) {
        CollaborationRoom room = roomService.getById(roomId);
        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setDeadline(LocalDateTime.now().plusHours(2));
        request.setPointsReward(10);

        switch (room.getLevel()) {
            case BEGINNER -> {
                request.setType(ChallengeType.FILL_BLANK);
                request.setDifficulty(ChallengeDifficulty.EASY);
                String[] beginnerSentences = {
                        "The sky ___ blue. (is / are / am)",
                        "She ___ a student. (is / be / are)",
                        "They ___ playing football. (are / is / am)",
                        "I ___ hungry right now. (am / is / are)",
                        "He ___ his homework every evening. (does / do / did)",
                        "We ___ to school by bus. (go / goes / went)",
                        "The cat ___ on the sofa. (sits / sit / sat)",
                        "My mother ___ coffee in the morning. (drinks / drink / drank)",
                        "The children ___ very happy today. (are / is / am)",
                        "It ___ raining outside. (is / are / be)",
                        "She ___ a book every week. (reads / read / reading)",
                        "I ___ English at school. (learn / learns / learning)",
                        "He ___ to work by car. (drives / drive / drove)",
                        "They ___ dinner at seven o'clock. (have / has / had)",
                        "The dog ___ very loud. (barks / bark / barked)",
                        "We ___ a new house last year. (bought / buy / buys)",
                        "She ___ her friends on weekends. (meets / meet / met)",
                        "The sun ___ in the east. (rises / rise / rose)",
                        "I ___ not like spicy food. (do / does / did)",
                        "He ___ tired after a long day. (feels / feel / felt)"
                };
                String picked = beginnerSentences[(int)(Math.random() * beginnerSentences.length)];
                request.setPrompt("Fill in the blank with the correct word: \"" + picked + "\"");
                String answer = picked.substring(picked.indexOf("(") + 1, picked.indexOf("/")).trim();
                request.setCorrectAnswer(answer);
            }
            case INTERMEDIATE -> {
                request.setType(ChallengeType.TRANSLATION_RACE);
                request.setDifficulty(ChallengeDifficulty.MEDIUM);
                String[][] translationPairs = {
                        {"J'apprends l'anglais tous les jours.", "I am learning English every day."},
                        {"Elle va à l'école en bus le matin.", "She goes to school by bus in the morning."},
                        {"Nous étudions pour l'examen final cette semaine.", "We are studying for the final exam this week."},
                        {"Il ne comprend pas la question très bien.", "He does not understand the question very well."},
                        {"Ils vivent dans cette ville depuis cinq ans.", "They have been living in this city for five years."},
                        {"Mon ami prévoit un voyage à Londres le mois prochain.", "My friend is planning a trip to London next month."},
                        {"Le professeur a expliqué la règle de grammaire clairement.", "The teacher explained the grammar rule clearly."},
                        {"J'ai oublié d'apporter mes devoirs en classe aujourd'hui.", "I forgot to bring my homework to class today."},
                        {"Elle lit un livre intéressant sur l'histoire.", "She is reading an interesting book about history."},
                        {"Nous devons terminer ce projet avant la date limite.", "We need to finish this project before the deadline."},
                        {"Il a appelé sa mère deux fois hier.", "He called his mother twice yesterday."},
                        {"La réunion a été annulée en raison du mauvais temps.", "The meeting was cancelled due to bad weather."},
                        {"Je n'ai jamais voyagé en dehors de mon pays avant.", "I have never travelled outside of my country before."},
                        {"Elle parle trois langues couramment.", "She speaks three languages fluently."},
                        {"Ils ont décidé de rester à la maison plutôt que de sortir.", "They decided to stay home instead of going out."},
                        {"Il attend le week-end avec impatience.", "He is looking forward to the weekend."},
                        {"Les étudiants ont posé beaucoup de questions pendant le cours.", "The students asked many questions during the lecture."},
                        {"Je bois habituellement du café avant de commencer à travailler.", "I usually drink coffee before starting work."},
                        {"Elle a oublié son parapluie et a été mouillée sous la pluie.", "She forgot her umbrella and got wet in the rain."},
                        {"Nous avons regardé un excellent film ensemble hier soir.", "We watched a great movie last night together."}
                };
                String[][] pair = new String[][]{ translationPairs[(int)(Math.random() * translationPairs.length)] };
                request.setPrompt("Translate to English: \"" + pair[0][0] + "\"");
                request.setCorrectAnswer(pair[0][1]);
            }
            case ADVANCED -> {
                request.setType(ChallengeType.STORY_BUILDER);
                request.setDifficulty(ChallengeDifficulty.HARD);
                String[] advancedOpeners = {
                        "The unexpected letter changed everything she thought she knew...",
                        "Nobody believed him until the day he proved them all wrong...",
                        "She had always avoided that old house at the end of the street...",
                        "The city was silent, unusually so, on that particular morning...",
                        "He opened the box and immediately regretted his curiosity...",
                        "It was the last train of the night, and she was the only passenger...",
                        "The map led them to a place that no GPS could find...",
                        "After ten years abroad, he finally decided to return home...",
                        "She received a message from a number she did not recognize...",
                        "The experiment had worked, but not in the way they expected...",
                        "They had been friends since childhood, until that summer changed everything...",
                        "The decision seemed simple at first, but nothing about it was simple...",
                        "He had memorised every detail of the plan, yet something still went wrong...",
                        "The door was open, the lights were on, but the room was completely empty...",
                        "She was certain she had never met him before, yet he knew her name...",
                        "The competition had only one rule, and he had already broken it...",
                        "It had taken her three years to build it, and only three seconds to lose it...",
                        "The photograph on the wall showed a family, but none of them were smiling...",
                        "By the time they realised what was happening, it was already too late...",
                        "He wrote the same word every day in his notebook, always in red ink..."
                };
                String picked = advancedOpeners[(int)(Math.random() * advancedOpeners.length)];
                request.setPrompt("Story Builder — continue this story using at least 3 sentences and advanced vocabulary: \"" + picked + "\"");
                request.setCorrectAnswer("");
            }
        }

        return challengeService.create(roomId, currentUserId, request);
    }
}
