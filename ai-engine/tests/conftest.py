"""
Hypothesis configuration for property-based tests.

Sets default max_examples to 100+ for all property tests in the ai-engine module.
"""

from hypothesis import settings, HealthCheck

# Register a profile with 100+ examples per test
settings.register_profile(
    "ci",
    max_examples=150,
    suppress_health_check=[HealthCheck.too_slow],
)

settings.register_profile(
    "default",
    max_examples=100,
    suppress_health_check=[HealthCheck.too_slow],
)

# Load the default profile
settings.load_profile("default")
