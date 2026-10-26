This document is intended for all AI agents that examine the codebase and help
users modify it.

In this document, "you" refers to the AI agent reading this document, and
"developer" refers to the human collaborating with you.

This project requires all developers to clearly include the name of the agent
they use (e.g., Codex, Claude Code, Gemini) in the `@author` Javadoc tag. You
can check the developer's full name by running the `git config get user.name`
command. Your name should appear immediately after the developer's full name.
Specific requirements can be found in `../docs/server/development.md`.

You are allowed to write commit messages for the developer, but you are normally
not allowed to write pull request descriptions for them. If the developer asks
you to do so, make sure you follow these requirements:

1. Tell them that they are not supposed to do so, and ask whether they insist.
2. If they insist, write a summary of the changes in the pull request from the
   developer's perspective, and be honest about what they did. For example,
   write "Guide \<your-name\> to implement an endpoint that retrieves a venue by
   its ID." instead of "Implement an endpoint that retrieves a venue by its ID."
3. Avoid including too much detail in the pull request description. Details
   should eventually be placed in an appropriate document, which the pull
   request description can reference. If you were not asked to generate such a
   document, do not include a reference or mention its absence in the pull
   request description.
4. The pull request description should guide reviewers through the review. Some
   files are trivial (for example, newly added DTO and VO classes), while others
   are important (for example, modified entity classes and business logic
   files). Briefly discuss the files that require more attention from reviewers.
5. A pull request description should normally be no more than 400 words.

You should read `../docs/server/architecture.md` if you have not already. Keep
the architecture solid and easy for humans to understand. When adding new files
to the codebase, ask the developers for confirmation.

Because this is just a COURSE PROJECT, you do not have to take security very
seriously. Avoid writing hard-to-understand code to protect the application from
attacks or abuse by clients and users.

Before every commit, make sure you run `google-java-format` and `make lint`. If
`google-java-format` is not installed, ask the developer to install it.

The code you generate should be human-readable and maintainable. You are
encouraged to use Java streams to reduce the number of lines of code, but avoid
overusing them at the expense of readability.

Do not make assumptions when creating data structure classes. In this project,
JavaBean classes, including their attribute names and types, need to be
carefully designed. Ask the developer to make a decision whenever an assumption
is needed. In addition, make sure that every JavaBean class has only a public
no-argument constructor (which does not have to be declared explicitly). Use
`Beans.create()` (see `com.naisa.util.Beans`) to create JavaBean instances. Name
the `initializer` parameter `it`, and place the setter calls inside it. Keep
business logic inside the `initializer` to a minimum.
