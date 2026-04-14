package com.formfit.ai.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.ui.components.FormFitPrimaryButton
import com.formfit.ai.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FormFitNavy, GradientEnd)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingProgressBar(
                currentStep = uiState.currentStep,
                totalSteps = OnboardingStep.values().size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(top = 32.dp)
            )

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } togetherWith
                    slideOutHorizontally { width -> -width }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { step ->
                when (OnboardingStep.values()[step]) {
                    OnboardingStep.WELCOME -> WelcomeStep(
                        name = uiState.name,
                        onNameChange = viewModel::setName
                    )
                    OnboardingStep.GENDER -> GenderStep(
                        selectedGender = uiState.gender,
                        onGenderSelected = viewModel::setGender
                    )
                    OnboardingStep.GOAL -> GoalStep(
                        selectedGoal = uiState.goal,
                        onGoalSelected = viewModel::setGoal
                    )
                    OnboardingStep.FREQUENCY -> FrequencyStep(
                        selectedFrequency = uiState.frequency,
                        onFrequencySelected = viewModel::setFrequency
                    )
                    OnboardingStep.EQUIPMENT -> EquipmentStep(
                        selectedEquipment = uiState.equipment,
                        onEquipmentSelected = viewModel::setEquipment
                    )
                    OnboardingStep.REFERRAL -> ReferralStep(
                        selectedReferral = uiState.referralSource,
                        onReferralSelected = viewModel::setReferralSource
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 0) {
                    FormFitSecondaryButton(
                        text = "Back",
                        onClick = viewModel::previousStep,
                        modifier = Modifier.weight(0.35f)
                    )
                }

                FormFitPrimaryButton(
                    text = if (uiState.currentStep == OnboardingStep.values().size - 1) "Get Started 🚀" else "Continue",
                    onClick = viewModel::nextStep,
                    enabled = uiState.canProceed,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(if (uiState.currentStep > 0) 0.65f else 1f)
                )
            }
        }
    }
}

@Composable
fun OnboardingProgressBar(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (i <= currentStep) FormFitGreen else FormFitSurface3
                    )
            )
        }
    }
}

@Composable
fun WelcomeStep(name: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "👋", fontSize = 64.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Welcome to\nFormFit AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Let's personalize your experience.\nWhat's your name?",
            fontSize = 16.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Your name", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FormFitGreen,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = FormFitGreen,
                focusedContainerColor = FormFitSurface,
                unfocusedContainerColor = FormFitSurface
            )
        )
    }
}

@Composable
fun GenderStep(selectedGender: String, onGenderSelected: (String) -> Unit) {
    val options = listOf("Male" to "👨", "Female" to "👩", "Non-binary" to "🧑", "Prefer not to say" to "🤍")
    OnboardingChoiceStep(
        emoji = "⚧️",
        title = "What's your gender?",
        subtitle = "This helps us personalize your recommendations",
        options = options.map { it.first },
        optionEmojis = options.map { it.second },
        selectedOption = selectedGender,
        onOptionSelected = onGenderSelected
    )
}

@Composable
fun GoalStep(selectedGoal: String, onGoalSelected: (String) -> Unit) {
    val options = listOf("Lose Weight" to "🔥", "Build Muscle" to "💪", "Stay Active" to "⚡", "Injury Rehab" to "🏥")
    OnboardingChoiceStep(
        emoji = "🎯",
        title = "What's your fitness goal?",
        subtitle = "We'll tailor workouts to match your objective",
        options = options.map { it.first },
        optionEmojis = options.map { it.second },
        selectedOption = selectedGoal,
        onOptionSelected = onGoalSelected
    )
}

@Composable
fun FrequencyStep(selectedFrequency: String, onFrequencySelected: (String) -> Unit) {
    val options = listOf("1–2x per week" to "🌱", "3–4x per week" to "🔥", "5+ per week" to "💥")
    OnboardingChoiceStep(
        emoji = "📅",
        title = "How often do you work out?",
        subtitle = "We'll plan your rest days accordingly",
        options = options.map { it.first },
        optionEmojis = options.map { it.second },
        selectedOption = selectedFrequency,
        onOptionSelected = onFrequencySelected
    )
}

@Composable
fun EquipmentStep(selectedEquipment: String, onEquipmentSelected: (String) -> Unit) {
    val options = listOf("No Equipment" to "🏠", "Dumbbells" to "🏋️", "Full Gym" to "🏟️")
    OnboardingChoiceStep(
        emoji = "🏋️",
        title = "What equipment do you have?",
        subtitle = "We'll only show exercises you can actually do",
        options = options.map { it.first },
        optionEmojis = options.map { it.second },
        selectedOption = selectedEquipment,
        onOptionSelected = onEquipmentSelected
    )
}

@Composable
fun ReferralStep(selectedReferral: String, onReferralSelected: (String) -> Unit) {
    val options = listOf("Instagram" to "📸", "TikTok" to "🎵", "Friend/Family" to "👥", "Google Search" to "🔍", "App Store" to "📱", "YouTube" to "▶️")
    OnboardingChoiceStep(
        emoji = "💚",
        title = "How did you find us?",
        subtitle = "Just curious — helps us reach more people!",
        options = options.map { it.first },
        optionEmojis = options.map { it.second },
        selectedOption = selectedReferral,
        onOptionSelected = onReferralSelected
    )
}

@Composable
fun OnboardingChoiceStep(
    emoji: String,
    title: String,
    subtitle: String,
    options: List<String>,
    optionEmojis: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(text = emoji, fontSize = 56.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(text = title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(text = subtitle, fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) FormFitGreen.copy(alpha = 0.12f) else FormFitSurface
                    )
                    .then(
                        if (isSelected) Modifier.then(
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(FormFitGreen.copy(0.15f), FormFitGreen.copy(0.05f))
                                )
                            )
                        ) else Modifier
                    )
                    .clickable { onOptionSelected(option) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = optionEmojis[index], fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = option,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) FormFitGreen else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FormFitSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = FormFitGreen),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, FormFitGreen.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
