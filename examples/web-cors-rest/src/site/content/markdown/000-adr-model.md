
# 000 - Modelo ADR (Documenting Architecture Decisions)

Data: 08/11/2018

>Title These documents have names that are short noun phrases. For example, "ADR 1: Deployment on Ruby on Rails 3.0.10" or "ADR 9: LDAP for Multitenant Integration"

## Status
Aceito.

>Status A decision may be "proposed" if the project stakeholders haven't agreed with it yet, or "accepted" once it is agreed. If a later ADR changes or reverses a decision, it may be marked as "deprecated" or "superseded" with a reference to its replacement.

## Contexto

Mobile is the main platform today. We want to reach as many users as possible. Mobile use has two big constraints: battery life (and consequent sleeping of apps), and bandwidth because of costs Go platform development ignored mobile from the start and we found out late about the problems of compiling to mobile.

>Context This section describes the forces at play, including technological, political, social, and project local. These forces are probably in tension, and should be called out as such. The language in this section is value-neutral. It is simply describing facts.

## Decisão

Target a mobile build from the start. Do not initially worry about battery/bandwidth constraints, assuming that ADR 0006 will handle solve this issue in the medium term, and that advance in technology will handle it in the long-term.

>Decision This section describes our response to these forces. It is stated in full sentences, with active voice. "We will ..." Review: http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions

## Consequências

Have mobile testing in place and always test on mobile before releasing. Development has mobile platform performance considerations. Holo binding helps achieve this in a short-term way.

>Consequences This section describes the resulting context, after applying the decision. All consequences should be listed here, not just the "positive" ones. A particular decision may have positive, negative, and neutral consequences, but all of them affect the team and project in the future.


    The whole document should be one or two pages long. We will write each ADR as if it is a conversation with a future developer. This requires good writing style, with full sentences organized into paragraphs. Bullets are acceptable only for visual style, not as an excuse for writing sentence fragments. (Bullets kill people, even PowerPoint bullets.)
