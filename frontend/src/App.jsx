import { useState, useEffect, useCallback } from "react";
import "./App.css";

// Point these at your two services. Override via a .env file if you like:
// VITE_UPLOAD_URL=http://localhost:8081
// VITE_MATCH_URL=http://localhost:8082
const UPLOAD_URL = import.meta.env.VITE_UPLOAD_URL || "/upload";
const MATCH_URL = import.meta.env.VITE_MATCH_URL || "/match";

export default function App() {
  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState(null);
  const [selectedJobId, setSelectedJobId] = useState(null);
  const [matchResult, setMatchResult] = useState(null);
  const [matching, setMatching] = useState(false);
  const [matchError, setMatchError] = useState(null);

  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState(null);

  const [jobForm, setJobForm] = useState({ title: "", company: "", rawText: "" });
  const [creatingJob, setCreatingJob] = useState(false);
  const [jobError, setJobError] = useState(null);

  const refreshResumes = useCallback(() => {
    fetch(`${UPLOAD_URL}/api/v1/resumes`)
        .then((r) => r.json())
        .then(setResumes)
        .catch(() => {});
  }, []);

  const refreshJobs = useCallback(() => {
    fetch(`${UPLOAD_URL}/api/v1/jobs`)
        .then((r) => r.json())
        .then(setJobs)
        .catch(() => {});
  }, []);

  useEffect(() => {
    refreshResumes();
    refreshJobs();
  }, [refreshResumes, refreshJobs]);

  async function handleFileUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    setUploadError(null);
    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await fetch(`${UPLOAD_URL}/api/v1/resumes`, {
        method: "POST",
        body: formData,
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.error || `Upload failed (${res.status})`);
      }
      const saved = await res.json();
      setResumes((prev) => [saved, ...prev]);
      setSelectedResumeId(saved.id);
    } catch (err) {
      setUploadError(err.message);
    } finally {
      setUploading(false);
      e.target.value = "";
    }
  }

  async function handleCreateJob(e) {
    e.preventDefault();
    setCreatingJob(true);
    setJobError(null);
    try {
      const res = await fetch(`${UPLOAD_URL}/api/v1/jobs`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(jobForm),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(
            body.title || body.rawText || body.error || `Could not save job (${res.status})`
        );
      }
      const saved = await res.json();
      setJobs((prev) => [saved, ...prev]);
      setSelectedJobId(saved.id);
      setJobForm({ title: "", company: "", rawText: "" });
    } catch (err) {
      setJobError(err.message);
    } finally {
      setCreatingJob(false);
    }
  }

  async function handleRunMatch() {
    if (!selectedResumeId || !selectedJobId) return;
    setMatching(true);
    setMatchError(null);
    setMatchResult(null);
    try {
      const res = await fetch(`${MATCH_URL}/api/v1/match`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ resumeId: selectedResumeId, jobId: selectedJobId }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.error || `Match failed (${res.status})`);
      }
      setMatchResult(await res.json());
    } catch (err) {
      setMatchError(err.message);
    } finally {
      setMatching(false);
    }
  }

  return (
      <div className="page">
        <header className="topbar">
          <div className="mark">TS</div>
          <div>
            <h1>TalentSync</h1>
            <p className="subtitle">Resume-to-role fit, scored by AI</p>
          </div>
        </header>

        <main className="grid">
          {/* Column 1 — resumes */}
          <section className="panel">
            <div className="panel-head">
              <span className="eyebrow">01</span>
              <h2>Resumes</h2>
            </div>

            <label className="upload-box" htmlFor="resume-upload">
              <input
                  id="resume-upload"
                  type="file"
                  accept=".pdf,.txt"
                  onChange={handleFileUpload}
                  disabled={uploading}
                  hidden
              />
              <span className="upload-icon">＋</span>
              <span>{uploading ? "Uploading…" : "Upload resume (PDF or TXT)"}</span>
            </label>
            {uploadError && <p className="error">{uploadError}</p>}

            <ul className="pick-list">
              {resumes.length === 0 && <li className="empty">No resumes yet.</li>}
              {resumes.map((r) => (
                  <li
                      key={r.id}
                      className={r.id === selectedResumeId ? "pick-item active" : "pick-item"}
                      onClick={() => setSelectedResumeId(r.id)}
                  >
                    <span className="item-title">{r.fileName}</span>
                    <span className="item-meta">#{r.id}</span>
                  </li>
              ))}
            </ul>
          </section>

          {/* Column 2 — jobs */}
          <section className="panel">
            <div className="panel-head">
              <span className="eyebrow">02</span>
              <h2>Job descriptions</h2>
            </div>

            <form className="job-form" onSubmit={handleCreateJob}>
              <input
                  placeholder="Role title"
                  value={jobForm.title}
                  onChange={(e) => setJobForm({ ...jobForm, title: e.target.value })}
                  required
              />
              <input
                  placeholder="Company (optional)"
                  value={jobForm.company}
                  onChange={(e) => setJobForm({ ...jobForm, company: e.target.value })}
              />
              <textarea
                  placeholder="Paste the job description…"
                  rows={4}
                  value={jobForm.rawText}
                  onChange={(e) => setJobForm({ ...jobForm, rawText: e.target.value })}
                  required
              />
              <button type="submit" disabled={creatingJob}>
                {creatingJob ? "Saving…" : "Add job"}
              </button>
              {jobError && <p className="error">{jobError}</p>}
            </form>

            <ul className="pick-list">
              {jobs.length === 0 && <li className="empty">No jobs yet.</li>}
              {jobs.map((j) => (
                  <li
                      key={j.id}
                      className={j.id === selectedJobId ? "pick-item active" : "pick-item"}
                      onClick={() => setSelectedJobId(j.id)}
                  >
                    <span className="item-title">{j.title}</span>
                    <span className="item-meta">{j.company || `#${j.id}`}</span>
                  </li>
              ))}
            </ul>
          </section>

          {/* Column 3 — match */}
          <section className="panel result-panel">
            <div className="panel-head">
              <span className="eyebrow">03</span>
              <h2>Fit score</h2>
            </div>

            <button
                className="match-btn"
                onClick={handleRunMatch}
                disabled={!selectedResumeId || !selectedJobId || matching}
            >
              {matching ? "Analyzing…" : "Run match"}
            </button>
            {matchError && <p className="error">{matchError}</p>}

            {!matchResult && !matching && (
                <p className="empty result-empty">
                  Pick a resume and a job, then run the match.
                </p>
            )}

            {matchResult && (
                <div className="result">
                  <ScoreDial score={matchResult.fitScore} />

                  <div className="skill-block">
                    <h3>Matched skills</h3>
                    <div className="chips">
                      {matchResult.matchedSkills?.length ? (
                          matchResult.matchedSkills.map((s) => (
                              <span className="chip chip-match" key={s}>
                        {s}
                      </span>
                          ))
                      ) : (
                          <span className="empty">None found</span>
                      )}
                    </div>
                  </div>

                  <div className="skill-block">
                    <h3>Missing skills</h3>
                    <div className="chips">
                      {matchResult.missingSkills?.length ? (
                          matchResult.missingSkills.map((s) => (
                              <span className="chip chip-gap" key={s}>
                        {s}
                      </span>
                          ))
                      ) : (
                          <span className="empty">None — full coverage</span>
                      )}
                    </div>
                  </div>

                  <div className="skill-block">
                    <h3>Summary</h3>
                    <p className="gap-text">{matchResult.gapAnalysis}</p>
                  </div>
                </div>
            )}
          </section>
        </main>
      </div>
  );
}

function ScoreDial({ score }) {
  const clamped = Math.max(0, Math.min(100, score ?? 0));
  const angle = (clamped / 100) * 360;
  const tone = clamped >= 75 ? "high" : clamped >= 45 ? "mid" : "low";

  return (
      <div
          className={`dial dial-${tone}`}
          style={{ "--angle": `${angle}deg` }}
      >
        <div className="dial-inner">
          <span className="dial-number">{clamped}</span>
          <span className="dial-label">/ 100</span>
        </div>
      </div>
  );
}