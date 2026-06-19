(async () => {
  const video = document.getElementById('video');
  const canvas = document.getElementById('overlay');
  const ctx = canvas.getContext('2d');
  const exerciseEl = document.getElementById('exercise');
  const repsEl = document.getElementById('reps');
  const setsEl = document.getElementById('sets');
  const stateEl = document.getElementById('state');
  const restEl = document.getElementById('rest');
  const restTimeEl = document.getElementById('restTime');
  const startBtn = document.getElementById('startBtn');
  const stopBtn = document.getElementById('stopBtn');

  let detector;
  let running = false;
  let sessionState = 'Idle';
  let currentExercise = null;
  let repCount = 0;
  let setCount = 0;
  let repCounter = null;
  let restMs = 0;
  let restTimer = null;

  // Simple motion-phase counter
  class RepetitionCounter {
    constructor(type){
      this.type = type;
      this.count = 0;
      this.lastPhase = 'REST';
      this.consecutive = 0;
      this.framesNeeded = 3;
    }
    process(landmarks){
      const phase = detectPhase(this.type, landmarks);
      if(phase === this.lastPhase){
        this.consecutive = 0;
        return false;
      }
      this.consecutive++;
      if(this.consecutive >= this.framesNeeded){
        // Complete a rep when toggling between TOP and BOTTOM
        if((this.lastPhase === 'TOP' && phase === 'BOTTOM') || (this.lastPhase === 'BOTTOM' && phase === 'TOP')){
          this.count += 1;
          this.lastPhase = phase;
          this.consecutive = 0;
          return true;
        }
        this.lastPhase = phase;
        this.consecutive = 0;
      }
      return false;
    }
    getCount(){ return this.count; }
  }

  function detectPhase(type, kps){
    // kps: array of keypoints {x,y,score}
    if(!kps || kps.length === 0) return 'REST';
    const nose = kps[0];
    const leftHip = kps[23] || kps[11];
    const rightHip = kps[24] || kps[12];
    const leftKnee = kps[25];
    const leftAnkle = kps[27];
    const leftElbow = kps[7];
    const leftWrist = kps[9];

    if(type === 'PUSH_UPS'){
      // use elbow angle approximation via y-values
      if(!leftElbow || !leftWrist) return 'REST';
      const elbowY = leftElbow.y; const wristY = leftWrist.y;
      if(elbowY - wristY > 0.08) return 'BOTTOM';
      if(wristY - elbowY > 0.05) return 'TOP';
      return 'MID';
    }
    if(type === 'SQUATS'){
      if(!leftHip || !leftKnee || !leftAnkle) return 'REST';
      // knee angle proxy: vertical distances
      const hipY = leftHip.y; const kneeY = leftKnee.y; const ankleY = leftAnkle.y;
      const kneeBend = Math.abs(hipY - kneeY) + Math.abs(kneeY - ankleY);
      if(kneeBend > 0.55) return 'BOTTOM';
      if(kneeBend < 0.3) return 'TOP';
      return 'MID';
    }
    if(type === 'PULL_UPS'){
      if(!leftWrist || !kps[11]) return 'REST';
      const wristY = leftWrist.y; const shoulderY = kps[11].y;
      if(wristY < shoulderY - 0.08) return 'TOP';
      if(wristY > shoulderY + 0.08) return 'BOTTOM';
      return 'MID';
    }
    if(type === 'AB_EXERCISES'){
      if(!nose || !leftHip) return 'REST';
      const d = Math.hypot(nose.x - leftHip.x, nose.y - leftHip.y);
      if(d < 0.18) return 'TOP';
      if(d > 0.35) return 'BOTTOM';
      return 'MID';
    }
    return 'REST';
  }

  function classifyExercise(kps){
    if(!kps || kps.length === 0) return null;
    const leftWrist = kps[9]; const rightWrist = kps[10];
    const leftShoulder = kps[11]; const rightShoulder = kps[12];
    const nose = kps[0]; const leftHip = kps[23] || kps[11]; const rightHip = kps[24] || kps[12];

    // Pull-ups: wrists above shoulders and body vertical
    if(leftWrist && rightWrist && leftShoulder && rightShoulder){
      const wristsAbove = leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y;
      const bodyVertical = Math.abs((leftHip.x + rightHip.x)/2 - ((leftShoulder.x+rightShoulder.x)/2)) < 0.12;
      if(wristsAbove && bodyVertical) return 'PULL_UPS';
    }

    // Push-ups: body horizontal (nose and hips similar y) and wrists under nose
    if(nose && leftHip && rightHip && leftWrist && rightWrist){
      const hipsY = (leftHip.y + rightHip.y)/2;
      const bodyHorizontal = Math.abs(nose.y - hipsY) < 0.08;
      const wristsUnder = leftWrist.y > nose.y && rightWrist.y > nose.y;
      if(bodyHorizontal && wristsUnder) return 'PUSH_UPS';
    }

    // Squats: knees bent (approx via hip/knee/ankle)
    if(kps[25] && kps[26] && kps[27]){
      const kneeY = (kps[25].y + kps[26].y)/2; const hipY = (kps[23].y + kps[24].y)/2; const ankleY = (kps[27].y + kps[28].y)/2;
      if(kneeY > hipY + 0.15 && ankleY > kneeY + 0.05) return 'SQUATS';
    }

    // Ab exercises: lying (nose below hips or large distance)
    if(nose && leftHip){
      const dh = Math.abs(nose.y - leftHip.y);
      if(dh > 0.18) return 'AB_EXERCISES';
    }

    return null;
  }

  async function setupCamera(){
    if(!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) throw new Error('Camera not available');
    const stream = await navigator.mediaDevices.getUserMedia({video: {facingMode: 'user'}, audio: false});
    video.srcObject = stream;
    await video.play();
    canvas.width = video.videoWidth; canvas.height = video.videoHeight;
  }

  async function loadModel(){
    await tf.setBackend('webgl');
    const model = poseDetection.SupportedModels.MoveNet;
    const detectorConfig = {modelType: poseDetection.movenet.modelType.SINGLEPOSE_LIGHTNING};
    detector = await poseDetection.createDetector(model, detectorConfig);
  }

  // smoothing state for keypoints
  let prevKps = null;
  const SMOOTHING_ALPHA = 0.75; // higher = smoother

  function smoothKeypoints(kps){
    if(!kps) return kps;
    if(!prevKps){
      prevKps = kps.map(p => ({x: p.x, y: p.y, score: p.score}));
      return prevKps;
    }
    for(let i=0;i<kps.length;i++){
      const p = kps[i];
      if(!p) continue;
      if(p.score > 0.15){
        prevKps[i] = prevKps[i] || {x: p.x, y: p.y, score: p.score};
        prevKps[i].x = SMOOTHING_ALPHA * prevKps[i].x + (1 - SMOOTHING_ALPHA) * p.x;
        prevKps[i].y = SMOOTHING_ALPHA * prevKps[i].y + (1 - SMOOTHING_ALPHA) * p.y;
        prevKps[i].score = p.score;
      } else {
        // decay score if missing
        if(prevKps[i]) prevKps[i].score = Math.max(0, prevKps[i].score * 0.9);
      }
    }
    return prevKps;
  }

  const SKELETON = [
    [0,1],[0,2],[1,3],[2,4],[5,6],[5,7],[7,9],[6,8],[8,10],[11,12],[11,13],[13,15],[12,14],[14,16],[23,24],[11,23],[12,24],[23,25],[24,26],[25,27],[26,28]
  ];

  function drawSkeleton(kps){
    ctx.lineWidth = 2;
    for(const [a,b] of SKELETON){
      const p1 = kps[a]; const p2 = kps[b];
      if(!p1 || !p2) continue;
      if(p1.score > 0.2 && p2.score > 0.2){
        ctx.strokeStyle = 'rgba(0,200,120,0.9)';
        ctx.beginPath();
        ctx.moveTo(p1.x * canvas.width, p1.y * canvas.height);
        ctx.lineTo(p2.x * canvas.width, p2.y * canvas.height);
        ctx.stroke();
      }
    }
  }

  function drawKeypoints(keypoints){
    ctx.clearRect(0,0,canvas.width,canvas.height);
    if(!keypoints) return;
    drawSkeleton(keypoints);
    ctx.fillStyle = '#ff4757';
    keypoints.forEach(k => {
      if(k && k.score > 0.2){
        ctx.beginPath(); ctx.arc(k.x * canvas.width, k.y * canvas.height, 4, 0, Math.PI*2); ctx.fill();
      }
    });
  }

  async function frameLoop(){
    if(!running) return;
    const poses = await detector.estimatePoses(video, {flipHorizontal: true});
    const pose = poses && poses[0] ? poses[0] : null;
    let kps = pose ? pose.keypoints.map(k => ({x: k.x / video.videoWidth, y: k.y / video.videoHeight, score: k.score})) : null;
    kps = smoothKeypoints(kps);

    drawKeypoints(kps);

    const detected = classifyExercise(kps);
    // State machine
    if(sessionState === 'Detecting'){
      stateEl.textContent = 'Detecting';
      if(detected){
        currentExercise = detected;
        exerciseEl.textContent = humanName(currentExercise);
        repCounter = new RepetitionCounter(currentExercise);
        sessionState = 'SetInProgress';
        setCount += 1; setsEl.textContent = setCount;
      }
    } else if(sessionState === 'SetInProgress'){
      stateEl.textContent = 'SetInProgress';
      if(detected && detected !== currentExercise){
        // switch exercise
        currentExercise = detected;
        exerciseEl.textContent = humanName(currentExercise);
        repCounter = new RepetitionCounter(currentExercise);
        setCount += 1; setsEl.textContent = setCount;
      }
      if(kps){
        const repDone = repCounter.process(kps);
        if(repDone){
          repCount = repCounter.getCount(); repsEl.textContent = repCount;
          // voice feedback
          try{ window.speechSynthesis.speak(new SpeechSynthesisUtterance(repCount + ' повтор')); }catch(e){}
        }
      }
      // detect standing (end set)
      const standing = isStanding(kps);
      if(standing){
        sessionState = 'SetCompleted';
        stateEl.textContent = 'SetCompleted';
        startRest();
      }
    } else if(sessionState === 'SetCompleted'){
      // waiting to start rest
    } else if(sessionState === 'Resting'){
      stateEl.textContent = 'Resting';
      // rest handled by timer
    } else if(sessionState === 'Idle'){
      stateEl.textContent = 'Idle';
      if(detected){ sessionState = 'Detecting'; }
    }

    requestAnimationFrame(frameLoop);
  }

  function humanName(key){
    if(!key) return '—';
    if(key==='PUSH_UPS') return 'Отжимания';
    if(key==='SQUATS') return 'Приседания';
    if(key==='PULL_UPS') return 'Подтягивания';
    if(key==='AB_EXERCISES') return 'Упражнения на пресс';
    return key;
  }

  function isStanding(kps){
    if(!kps) return false;
    const leftHip = kps[23]; const rightHip = kps[24]; const leftAnkle = kps[27]; const rightAnkle = kps[28];
    if(!leftHip || !rightHip || !leftAnkle || !rightAnkle) return false;
    const bodyVertical = Math.abs(((leftHip.x + rightHip.x)/2) - ((leftAnkle.x + rightAnkle.x)/2)) < 0.12;
    const legsBelow = leftAnkle.y > leftHip.y && rightAnkle.y > rightHip.y;
    return bodyVertical && legsBelow;
  }

  function startRest(){
    sessionState = 'Resting';
    restMs = 60000; // 60s
    restEl.style.display = 'block';
    restTimeEl.textContent = Math.ceil(restMs/1000);
    restTimer = setInterval(() => {
      restMs -= 1000;
      restTimeEl.textContent = Math.ceil(restMs/1000);
      if(restMs <= 0){
        clearInterval(restTimer); restTimer = null; restEl.style.display = 'none'; sessionState='Idle'; stateEl.textContent='Idle';
      }
    }, 1000);
  }

  startBtn.addEventListener('click', async () => {
    try{
      if(!detector) await loadModel();
      await setupCamera();
      running = true; sessionState = 'Detecting';
      requestAnimationFrame(frameLoop);
    } catch (e){ alert('Ошибка: ' + e.message); }
  });

  stopBtn.addEventListener('click', () => {
    running = false; sessionState='Idle'; stateEl.textContent='Idle';
    if(video.srcObject){
      video.srcObject.getTracks().forEach(t => t.stop());
      video.srcObject = null;
    }
    if(restTimer){ clearInterval(restTimer); restTimer = null; restEl.style.display='none'; }
  });

})();
