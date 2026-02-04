import { useParams, Link } from '@mono-repo-v2/web-component-wrapper';

const mockMembers: Record<
  string,
  { name: string; role: string; phone: string; email: string }
> = {
  'ct-001': {
    name: 'Dr. Sarah Johnson',
    role: 'Primary Care Physician',
    phone: '555-0101',
    email: 'sjohnson@clinic.com',
  },
  'ct-002': {
    name: 'Dr. Michael Chen',
    role: 'Cardiologist',
    phone: '555-0102',
    email: 'mchen@clinic.com',
  },
  'ct-003': {
    name: 'Lisa Thompson',
    role: 'Care Coordinator',
    phone: '555-0103',
    email: 'lthompson@clinic.com',
  },
};

export function CareTeamMemberDetail() {
  const { id } = useParams<{ id: string }>();
  const member = id ? mockMembers[id] : null;

  if (!member) {
    return (
      <div className="member-not-found">
        <p>Member not found</p>
        <Link to="/">Back to Care Team</Link>
      </div>
    );
  }

  return (
    <div className="member-detail">
      <Link to="/" className="back-link">
        &larr; Back to Care Team
      </Link>
      <h3>{member.name}</h3>
      <p className="member-role">{member.role}</p>
      <div className="contact-info">
        <p>Phone: {member.phone}</p>
        <p>Email: {member.email}</p>
      </div>
    </div>
  );
}
