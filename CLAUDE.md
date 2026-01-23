# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## General Guidelines

### Testing
- Write tests for all new features and bug fixes
- Aim for high test coverage
- Tests should be comprehensive and cover edge cases
- Use appropriate testing frameworks (JUnit for Java, Jest for TypeScript)

### Error Handling
- Handle errors appropriately and provide meaningful error messages
- Use exceptions for exceptional conditions, not for control flow
- Validate inputs and fail fast
- Log errors with appropriate context

### Task Management
- Always check tasks in the task list when they are completed
- Update task lists in `docs/tasks.md` to reflect current progress
- Mark tasks as completed by changing `- [ ]` to `- [x]`
- Keep task lists up-to-date to help track project progress
- Ensure all subtasks are checked before marking a parent task as complete

## Essential Build Commands

```bash
# Build everything (compile + test)
mvn clean package

# Run tests only
mvn clean test

## Java Guidelines

### Code Style
- Follow standard Java naming conventions
  - Classes: PascalCase
  - Methods and variables: camelCase
  - Constants: UPPER_SNAKE_CASE
- Use 4 spaces for indentation
- Keep lines under 120 characters
- Use meaningful variable and method names

### Java Patterns
- Use Lombok annotations to reduce boilerplate (@Getter, @RequiredArgsConstructor, etc.)
- Use interfaces with default methods where appropriate
- Use @Nullable annotations for null safety
- Use functional programming style with streams and lambdas where it improves readability
- Prefer immutable objects where possible
- Use the Builder pattern for complex object creation
- Use the Visitor pattern for tree traversal and transformation

### Java Best Practices
- Favor composition over inheritance
- Use dependency injection for better testability
- Write small, focused methods
- Avoid mutable state where possible
- Use appropriate data structures for the task
- Follow the principle of least surprise

## Version Control Guidelines

### Commits
- Write clear, concise commit messages
- Each commit should represent a logical change
- Keep commits focused on a single task
- Reference issue numbers in commit messages where applicable

### Pull Requests
- Write a clear description of the changes
- Include tests for new features and bug fixes
- Ensure all tests pass before submitting
- Address review comments promptly

## Important Conventions

### Nullability Annotations
The project uses JSpecify nullability annotations.

## Conclusion

Following these guidelines will help maintain code quality and consistency. These guidelines are not exhaustive, and common sense should be applied when making decisions not covered here.
