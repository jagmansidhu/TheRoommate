------------------------------ MODULE RoomMembership ------------------------------
\* Model of RoomServiceImplt.createRoom and RoomServiceImplt.joinRoom.
\*
\* One step here is one step in those methods:
\*   Begin  = the unlocked read (countRoomsByUserId, members.size(), or alreadyMember)
\*   Commit = roomRepository.save after that read
\* Those are separate steps because the reads are plain queries. createRoom and
\* joinRoom are @Transactional, and this database's default isolation is
\* PostgreSQL READ COMMITTED, which does not lock the rows the count just saw.
\* ChecksAreAtomic is TRUE only when the checker finds a lock or SERIALIZABLE
\* on both methods. It is FALSE for the code this model was written against.
\*
\* A guard scenario starts already at the cap and has one caller. The action is
\* enabled only when that method's check is missing. A race scenario starts one
\* under the cap and has two callers, so both can pass the same read.

EXTENDS Integers

CONSTANTS
  Scenario,
  CreateChecksCap,
  JoinChecksUserCap,
  JoinChecksRoomCap,
  JoinChecksDuplicate,
  ChecksAreAtomic

ASSUME Scenario \in {
  "createUserCapRace", "createUserCapGuard",
  "joinUserCapRace", "joinUserCapGuard",
  "roomCapRace", "roomCapGuard",
  "dupRace", "dupGuard"
}
ASSUME CreateChecksCap \in BOOLEAN
ASSUME JoinChecksUserCap \in BOOLEAN
ASSUME JoinChecksRoomCap \in BOOLEAN
ASSUME JoinChecksDuplicate \in BOOLEAN
ASSUME ChecksAreAtomic \in BOOLEAN

IsGuardScenario == Scenario \in {
  "createUserCapGuard", "joinUserCapGuard", "roomCapGuard", "dupGuard"
}

Procs == IF IsGuardScenario THEN {"p1"} ELSE {"p1", "p2"}

GuardOn ==
  CASE Scenario \in {"createUserCapRace", "createUserCapGuard"} -> CreateChecksCap
    [] Scenario \in {"joinUserCapRace", "joinUserCapGuard"} -> JoinChecksUserCap
    [] Scenario \in {"roomCapRace", "roomCapGuard"} -> JoinChecksRoomCap
    [] Scenario \in {"dupRace", "dupGuard"} -> JoinChecksDuplicate

Limit ==
  CASE Scenario \in {"createUserCapRace", "createUserCapGuard", "joinUserCapRace", "joinUserCapGuard"} -> 3
    [] Scenario \in {"roomCapRace", "roomCapGuard"} -> 6
    [] Scenario \in {"dupRace", "dupGuard"} -> 1

InitCount ==
  CASE Scenario = "createUserCapRace" -> 2
    [] Scenario = "createUserCapGuard" -> 3
    [] Scenario = "joinUserCapRace" -> 2
    [] Scenario = "joinUserCapGuard" -> 3
    [] Scenario = "roomCapRace" -> 5
    [] Scenario = "roomCapGuard" -> 6
    [] Scenario = "dupRace" -> 0
    [] Scenario = "dupGuard" -> 1

VARIABLES count, pc

Init ==
  /\ count = InitCount
  /\ pc = [p \in Procs |-> "idle"]

CanPass ==
  \/ ~GuardOn
  \/ count < Limit

Begin(p) ==
  /\ pc[p] = "idle"
  /\ CanPass
  /\ IF ChecksAreAtomic
     THEN /\ count' = count + 1
          /\ pc' = [pc EXCEPT ![p] = "done"]
     ELSE /\ pc' = [pc EXCEPT ![p] = "checked"]
          /\ UNCHANGED count

Commit(p) ==
  /\ ~ChecksAreAtomic
  /\ pc[p] = "checked"
  /\ count' = count + 1
  /\ pc' = [pc EXCEPT ![p] = "done"]

Next == \E p \in Procs : Begin(p) \/ Commit(p)

Spec == Init /\ [][Next]_<<count, pc>>

\* The product promise modeled for this scenario.
CapOk == count <= Limit

TypeOk ==
  /\ count \in 0..12
  /\ pc \in [Procs -> {"idle", "checked", "done"}]

=============================================================================
