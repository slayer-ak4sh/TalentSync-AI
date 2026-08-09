package com.resumematcher.matching_service.service;

public class PromptBuilder {

    public static String buildMatchPrompt(String resumeText, String jobText) {
        return """
            You are a resume-to-job matching engine. Analyze the resume and job description below.

            Respond ONLY with valid JSON in exactly this format, with no markdown formatting, no code fences, and no extra text:

            {
              "matchedSkills": ["skill1", "skill2"],
              "missingSkills": ["skill1", "skill2"],
              "fitScore": 75,
              "gapAnalysis": "A 2-3 sentence plain-English summary of the candidate's fit for this role."
            }

            Rules:
            - matchedSkills: skills/technologies present in BOTH the resume and job description
            - missingSkills: skills/technologies required by the job but NOT found in the resume
            - fitScore: integer 0-100 representing overall fit
            - gapAnalysis: concise, actionable, written for the candidate to read

            RESUME:
            %s

            JOB DESCRIPTION:
            %s
            """.formatted(resumeText, jobText);
    }
}
